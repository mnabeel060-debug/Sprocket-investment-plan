package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    INVESTOR,
    ADMIN
}

enum class InvestmentStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class TransactionType {
    DEPOSIT,
    INVESTMENT,
    PROFIT,
    LOSS,
    WITHDRAWAL,
    REFUND
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    PROCESSING,
    PAID,
    REJECTED
}

enum class PaymentMethod {
    EASYPAISA,
    BANK_TRANSFER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val mobileNumber: String,
    val password: String,
    val investorId: String,
    val role: UserRole = UserRole.INVESTOR,
    val virtualBalance: Double = 0.0,
    val totalInvested: Double = 0.0,
    val totalProfitEarned: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "investment_packages")
data class InvestmentPackageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val minInvestment: Double,
    val maxInvestment: Double,
    val durationDays: Int = 7,
    val targetProfitPercent: Double = 20.0,
    val description: String,
    val isAvailable: Boolean = true
)

@Entity(tableName = "user_investments")
data class UserInvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val packageId: String,
    val packageName: String,
    val investedAmount: Double,
    val startDate: Long = System.currentTimeMillis(),
    val maturityDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val actualProfitOrLoss: Double = 0.0,
    val status: InvestmentStatus = InvestmentStatus.ACTIVE
)

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val investorName: String,
    val investorId: String,
    val amount: Double,
    val packageName: String,
    val paymentMethod: PaymentMethod = PaymentMethod.EASYPAISA,
    val senderNumber: String,
    val transactionId: String,
    val receiptNote: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val investorName: String,
    val investorId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val accountHolder: String,
    val accountNumber: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val referenceNumber: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
