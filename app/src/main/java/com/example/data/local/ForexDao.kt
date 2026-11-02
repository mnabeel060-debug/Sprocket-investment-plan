package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DepositRequestEntity
import com.example.data.model.InvestmentPackageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.RequestStatus
import com.example.data.model.SystemSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserInvestmentEntity
import com.example.data.model.WithdrawalRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForexDao {

    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET virtualBalance = virtualBalance + :amount WHERE id = :userId")
    suspend fun creditBalance(userId: Long, amount: Double)

    @Query("UPDATE users SET virtualBalance = virtualBalance - :amount WHERE id = :userId")
    suspend fun debitBalance(userId: Long, amount: Double)

    @Query("UPDATE users SET totalInvested = totalInvested + :amount WHERE id = :userId")
    suspend fun addInvestedCapital(userId: Long, amount: Double)

    @Query("UPDATE users SET totalProfitEarned = totalProfitEarned + :profit WHERE id = :userId")
    suspend fun addProfitEarned(userId: Long, profit: Double)

    // Investment Packages
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<InvestmentPackageEntity>)

    @Query("SELECT * FROM investment_packages WHERE isAvailable = 1")
    fun observePackages(): Flow<List<InvestmentPackageEntity>>

    @Query("SELECT * FROM investment_packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: String): InvestmentPackageEntity?

    @Update
    suspend fun updatePackage(pkg: InvestmentPackageEntity)

    // User Investments
    @Insert
    suspend fun insertInvestment(investment: UserInvestmentEntity): Long

    @Update
    suspend fun updateInvestment(investment: UserInvestmentEntity)

    @Query("SELECT * FROM user_investments WHERE userId = :userId ORDER BY startDate DESC")
    fun observeInvestmentsByUser(userId: Long): Flow<List<UserInvestmentEntity>>

    @Query("SELECT * FROM user_investments ORDER BY startDate DESC")
    fun observeAllInvestments(): Flow<List<UserInvestmentEntity>>

    @Query("SELECT * FROM user_investments WHERE status = 'ACTIVE'")
    suspend fun getAllActiveInvestments(): List<UserInvestmentEntity>

    // Deposits
    @Insert
    suspend fun insertDeposit(deposit: DepositRequestEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositRequestEntity)

    @Query("SELECT * FROM deposit_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeDepositsByUser(userId: Long): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests ORDER BY createdAt DESC")
    fun observeAllDeposits(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): DepositRequestEntity?

    @Query("SELECT COUNT(*) FROM deposit_requests WHERE userId = :userId AND status = 'PENDING'")
    fun observePendingDepositsCount(userId: Long): Flow<Int>

    // Withdrawals
    @Insert
    suspend fun insertWithdrawal(withdrawal: WithdrawalRequestEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalRequestEntity)

    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeWithdrawalsByUser(userId: Long): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests ORDER BY createdAt DESC")
    fun observeAllWithdrawals(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalRequestEntity?

    @Query("SELECT COUNT(*) FROM withdrawal_requests WHERE userId = :userId AND status = 'PENDING'")
    fun observePendingWithdrawalsCount(userId: Long): Flow<Int>

    // Transactions
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeTransactionsByUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    // Notifications
    @Insert
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeNotificationsByUser(userId: Long): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: Long)

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SystemSettingEntity)

    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SystemSettingEntity?

    @Query("SELECT * FROM system_settings")
    fun observeSettings(): Flow<List<SystemSettingEntity>>
}
