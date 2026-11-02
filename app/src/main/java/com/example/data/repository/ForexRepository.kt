package com.example.data.repository

import com.example.data.local.ForexDao
import com.example.data.model.DepositRequestEntity
import com.example.data.model.InvestmentPackageEntity
import com.example.data.model.InvestmentStatus
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.RequestStatus
import com.example.data.model.SystemSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserInvestmentEntity
import com.example.data.model.UserRole
import com.example.data.model.WithdrawalRequestEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class ForexRepository(private val dao: ForexDao) {

    // Auth & Users
    suspend fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        pass: String
    ): Result<UserEntity> {
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("Email already registered"))
        }
        val investorId = "INV-${Random.nextInt(100000, 999999)}"
        val newUser = UserEntity(
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            mobileNumber = phone.trim(),
            password = pass,
            investorId = investorId,
            role = UserRole.INVESTOR,
            virtualBalance = 100.0 // Starting demo balance gift for paper simulation
        )
        val id = dao.insertUser(newUser)
        val created = newUser.copy(id = id)

        // Welcome notification
        dao.insertNotification(
            NotificationEntity(
                userId = id,
                title = "Registration Successful",
                message = "Welcome $fullName! Your unique Investor ID is $investorId. A $100 demo balance has been credited to start your simulation."
            )
        )
        // Record registration bonus transaction
        dao.insertTransaction(
            TransactionEntity(
                userId = id,
                referenceNumber = "BONUS-${Random.nextInt(10000, 99999)}",
                type = TransactionType.DEPOSIT,
                amount = 100.0,
                description = "Starter Demo Balance Credit"
            )
        )

        return Result.success(created)
    }

    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val user = dao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(Exception("User not found"))
        if (user.password != pass) {
            return Result.failure(Exception("Invalid password"))
        }
        return Result.success(user)
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = dao.observeAllUsers()

    // Packages
    fun observePackages(): Flow<List<InvestmentPackageEntity>> = dao.observePackages()
    suspend fun updatePackage(pkg: InvestmentPackageEntity) = dao.updatePackage(pkg)

    // Buy Investment Package from Balance
    suspend fun activatePackageFromBalance(userId: Long, pkg: InvestmentPackageEntity): Result<Unit> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        if (user.virtualBalance < pkg.minInvestment) {
            return Result.failure(Exception("Insufficient virtual balance ($${String.format("%.2f", user.virtualBalance)}). Please submit a demo deposit first."))
        }

        dao.debitBalance(userId, pkg.minInvestment)
        dao.addInvestedCapital(userId, pkg.minInvestment)

        val investment = UserInvestmentEntity(
            userId = userId,
            packageId = pkg.id,
            packageName = "${pkg.name} ($${pkg.minInvestment.toInt()})",
            investedAmount = pkg.minInvestment,
            startDate = System.currentTimeMillis(),
            maturityDate = System.currentTimeMillis() + (pkg.durationDays * 24L * 60 * 60 * 1000)
        )
        dao.insertInvestment(investment)

        val ref = "INV-PKG-${Random.nextInt(100000, 999999)}"
        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                referenceNumber = ref,
                type = TransactionType.INVESTMENT,
                amount = pkg.minInvestment,
                description = "Purchased ${pkg.name} Package ($${pkg.minInvestment.toInt()})"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Investment Activated",
                message = "Your ${pkg.name} Package ($${pkg.minInvestment.toInt()}) is now active. Duration: 7 days."
            )
        )

        return Result.success(Unit)
    }

    // Submit Deposit Request
    suspend fun submitDepositRequest(
        userId: Long,
        amount: Double,
        packageName: String,
        paymentMethod: PaymentMethod,
        senderNumber: String,
        txnId: String,
        receiptNote: String
    ): Result<Long> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val req = DepositRequestEntity(
            userId = userId,
            investorName = user.fullName,
            investorId = user.investorId,
            amount = amount,
            packageName = packageName,
            paymentMethod = paymentMethod,
            senderNumber = senderNumber,
            transactionId = txnId,
            receiptNote = receiptNote,
            status = RequestStatus.PENDING
        )
        val id = dao.insertDeposit(req)

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Deposit Submitted",
                message = "Deposit request of $${amount.toInt()} via $paymentMethod (Txn: $txnId) has been submitted for admin verification."
            )
        )
        return Result.success(id)
    }

    // Admin approve deposit
    suspend fun approveDeposit(depositId: Long): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        if (deposit.status != RequestStatus.PENDING) {
            return Result.failure(Exception("Deposit is not pending"))
        }

        dao.updateDeposit(deposit.copy(status = RequestStatus.APPROVED))
        dao.creditBalance(deposit.userId, deposit.amount)

        val ref = "DEP-${deposit.transactionId.ifBlank { UUID.randomUUID().toString().take(8).uppercase() }}"
        dao.insertTransaction(
            TransactionEntity(
                userId = deposit.userId,
                referenceNumber = ref,
                type = TransactionType.DEPOSIT,
                amount = deposit.amount,
                description = "Approved Deposit via ${deposit.paymentMethod} ($${deposit.amount.toInt()})"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = deposit.userId,
                title = "Deposit Approved",
                message = "Your deposit of $${deposit.amount.toInt()} has been verified and added to your available virtual balance."
            )
        )

        return Result.success(Unit)
    }

    // Admin reject deposit
    suspend fun rejectDeposit(depositId: Long): Result<Unit> {
        val deposit = dao.getDepositById(depositId) ?: return Result.failure(Exception("Deposit not found"))
        dao.updateDeposit(deposit.copy(status = RequestStatus.REJECTED))

        dao.insertNotification(
            NotificationEntity(
                userId = deposit.userId,
                title = "Deposit Rejected",
                message = "Your deposit request for $${deposit.amount.toInt()} was rejected by admin. Please verify transaction details."
            )
        )
        return Result.success(Unit)
    }

    // Submit Withdrawal Request
    suspend fun submitWithdrawalRequest(
        userId: Long,
        amount: Double,
        method: PaymentMethod,
        accountHolder: String,
        accountNumber: String
    ): Result<Long> {
        val user = dao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        if (amount <= 0) return Result.failure(Exception("Invalid withdrawal amount"))
        if (user.virtualBalance < amount) {
            return Result.failure(Exception("Insufficient available balance ($${String.format("%.2f", user.virtualBalance)})."))
        }

        // Deduct from balance upfront
        dao.debitBalance(userId, amount)

        val req = WithdrawalRequestEntity(
            userId = userId,
            investorName = user.fullName,
            investorId = user.investorId,
            amount = amount,
            paymentMethod = method,
            accountHolder = accountHolder,
            accountNumber = accountNumber,
            status = RequestStatus.PENDING
        )
        val id = dao.insertWithdrawal(req)

        val ref = "WTH-REQ-${Random.nextInt(100000, 999999)}"
        dao.insertTransaction(
            TransactionEntity(
                userId = userId,
                referenceNumber = ref,
                type = TransactionType.WITHDRAWAL,
                amount = amount,
                description = "Withdrawal Request via $method ($accountNumber)"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Withdrawal Submitted",
                message = "Withdrawal request of $${String.format("%.2f", amount)} submitted. Status: Pending admin verification."
            )
        )

        return Result.success(id)
    }

    // Admin update withdrawal status
    suspend fun updateWithdrawalStatus(withdrawalId: Long, newStatus: RequestStatus): Result<Unit> {
        val item = dao.getWithdrawalById(withdrawalId) ?: return Result.failure(Exception("Not found"))
        dao.updateWithdrawal(item.copy(status = newStatus))

        when (newStatus) {
            RequestStatus.PAID -> {
                dao.insertNotification(
                    NotificationEntity(
                        userId = item.userId,
                        title = "Withdrawal Paid",
                        message = "Your withdrawal of $${item.amount} to ${item.accountNumber} has been successfully dispatched."
                    )
                )
            }
            RequestStatus.REJECTED -> {
                // Refund to balance
                dao.creditBalance(item.userId, item.amount)
                dao.insertTransaction(
                    TransactionEntity(
                        userId = item.userId,
                        referenceNumber = "REFUND-${Random.nextInt(100000, 999999)}",
                        type = TransactionType.REFUND,
                        amount = item.amount,
                        description = "Withdrawal Rejected & Refunded to Balance"
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        userId = item.userId,
                        title = "Withdrawal Rejected & Refunded",
                        message = "Your withdrawal request of $${item.amount} was rejected. Funds have been restored to your balance."
                    )
                )
            }
            RequestStatus.APPROVED -> {
                dao.insertNotification(
                    NotificationEntity(
                        userId = item.userId,
                        title = "Withdrawal Approved",
                        message = "Your withdrawal request for $${item.amount} has been approved and moved to processing."
                    )
                )
            }
            else -> {}
        }
        return Result.success(Unit)
    }

    // Admin Trading Session Profit/Loss Distribution
    suspend fun distributeTradingProfitLoss(
        percentChange: Double,
        sessionNote: String
    ): Result<Int> {
        val activeInvestments = dao.getAllActiveInvestments()
        if (activeInvestments.isEmpty()) {
            return Result.failure(Exception("No active investments found to distribute returns."))
        }

        var affectedCount = 0
        for (inv in activeInvestments) {
            val delta = inv.investedAmount * (percentChange / 100.0)
            val updatedProfitLoss = inv.actualProfitOrLoss + delta
            dao.updateInvestment(inv.copy(actualProfitOrLoss = updatedProfitLoss))

            if (delta > 0) {
                // Credit profit to user balance and total profit earned
                dao.creditBalance(inv.userId, delta)
                dao.addProfitEarned(inv.userId, delta)

                dao.insertTransaction(
                    TransactionEntity(
                        userId = inv.userId,
                        referenceNumber = "PFT-${Random.nextInt(100000, 999999)}",
                        type = TransactionType.PROFIT,
                        amount = delta,
                        description = "Simulated Trading Profit (+${String.format("%.1f", percentChange)}%) - $sessionNote"
                    )
                )

                dao.insertNotification(
                    NotificationEntity(
                        userId = inv.userId,
                        title = "Profit Credited",
                        message = "Your ${inv.packageName} earned +$${String.format("%.2f", delta)} (${String.format("%.1f", percentChange)}% return). Credited to balance."
                    )
                )
            } else if (delta < 0) {
                val lossAmount = kotlin.math.abs(delta)
                dao.insertTransaction(
                    TransactionEntity(
                        userId = inv.userId,
                        referenceNumber = "LSS-${Random.nextInt(100000, 999999)}",
                        type = TransactionType.LOSS,
                        amount = lossAmount,
                        description = "Simulated Trading Drawdown (${String.format("%.1f", percentChange)}%) - $sessionNote"
                    )
                )

                dao.insertNotification(
                    NotificationEntity(
                        userId = inv.userId,
                        title = "Trading Market Update",
                        message = "Simulated drawdown of -$${String.format("%.2f", lossAmount)} recorded for ${inv.packageName} during trading session."
                    )
                )
            }
            affectedCount++
        }

        return Result.success(affectedCount)
    }

    // System Settings
    suspend fun saveSetting(key: String, value: String) {
        dao.insertSetting(SystemSettingEntity(key, value))
    }

    fun observeSettings(): Flow<List<SystemSettingEntity>> = dao.observeSettings()

    // Observables
    fun observeInvestments(userId: Long) = dao.observeInvestmentsByUser(userId)
    fun observeAllInvestments() = dao.observeAllInvestments()

    fun observeDeposits(userId: Long) = dao.observeDepositsByUser(userId)
    fun observeAllDeposits() = dao.observeAllDeposits()

    fun observeWithdrawals(userId: Long) = dao.observeWithdrawalsByUser(userId)
    fun observeAllWithdrawals() = dao.observeAllWithdrawals()

    fun observeTransactions(userId: Long) = dao.observeTransactionsByUser(userId)
    fun observeAllTransactions() = dao.observeAllTransactions()

    fun observeNotifications(userId: Long) = dao.observeNotificationsByUser(userId)
    suspend fun markNotificationsRead(userId: Long) = dao.markAllNotificationsAsRead(userId)
}
