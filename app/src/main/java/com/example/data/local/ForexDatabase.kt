package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DepositRequestEntity
import com.example.data.model.InvestmentPackageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SystemSettingEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserInvestmentEntity
import com.example.data.model.UserRole
import com.example.data.model.WithdrawalRequestEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        InvestmentPackageEntity::class,
        UserInvestmentEntity::class,
        DepositRequestEntity::class,
        WithdrawalRequestEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        SystemSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ForexDatabase : RoomDatabase() {

    abstract fun forexDao(): ForexDao

    companion object {
        @Volatile
        private var INSTANCE: ForexDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ForexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForexDatabase::class.java,
                    "forex_simulator_db"
                )
                    .addCallback(ForexDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ForexDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.forexDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: ForexDao) {
            // Seed Packages
            val defaultPackages = listOf(
                InvestmentPackageEntity(
                    id = "starter",
                    name = "Starter",
                    minInvestment = 50.0,
                    maxInvestment = 50.0,
                    durationDays = 7,
                    targetProfitPercent = 20.0,
                    description = "Ideal entry point for beginner market analysts to test trading cycles with $50 capital."
                ),
                InvestmentPackageEntity(
                    id = "basic",
                    name = "Basic",
                    minInvestment = 100.0,
                    maxInvestment = 100.0,
                    durationDays = 7,
                    targetProfitPercent = 20.0,
                    description = "Balanced portfolio strategy package with $100 allocated for major currency pair scalping."
                ),
                InvestmentPackageEntity(
                    id = "standard",
                    name = "Standard",
                    minInvestment = 250.0,
                    maxInvestment = 250.0,
                    durationDays = 7,
                    targetProfitPercent = 20.0,
                    description = "Intermediate multi-pair strategy with $250 capital across EUR/USD, GBP/USD, and Gold."
                ),
                InvestmentPackageEntity(
                    id = "premium",
                    name = "Premium",
                    minInvestment = 500.0,
                    maxInvestment = 500.0,
                    durationDays = 7,
                    targetProfitPercent = 20.0,
                    description = "Advanced algorithmic simulated execution with $500 capital and daily volatility tracking."
                ),
                InvestmentPackageEntity(
                    id = "vip",
                    name = "VIP",
                    minInvestment = 1000.0,
                    maxInvestment = 1000.0,
                    durationDays = 7,
                    targetProfitPercent = 20.0,
                    description = "Comprehensive high-tier simulation suite with $1,000 capital and maximum volume testing."
                )
            )
            dao.insertPackages(defaultPackages)

            // Seed Admin User
            val adminUser = UserEntity(
                fullName = "Forex Desk Admin",
                email = "admin@forex.local",
                mobileNumber = "0300-0000000",
                password = "admin",
                investorId = "ADM-001",
                role = UserRole.ADMIN,
                virtualBalance = 100000.0
            )
            dao.insertUser(adminUser)

            // Seed Demo Investor User
            val demoInvestor = UserEntity(
                fullName = "Nabeel Investor",
                email = "investor@forex.local",
                mobileNumber = "0313-5445000",
                password = "user123",
                investorId = "INV-784012",
                role = UserRole.INVESTOR,
                virtualBalance = 250.0,
                totalInvested = 150.0,
                totalProfitEarned = 32.5
            )
            val investorId = dao.insertUser(demoInvestor)

            // Seed an active investment for demo investor
            val activeInvestment = UserInvestmentEntity(
                userId = investorId,
                packageId = "basic",
                packageName = "Basic ($100)",
                investedAmount = 100.0,
                startDate = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000), // 2 days ago
                maturityDate = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000), // 5 days left
                actualProfitOrLoss = 14.80
            )
            dao.insertInvestment(activeInvestment)

            val starterInvestment = UserInvestmentEntity(
                userId = investorId,
                packageId = "starter",
                packageName = "Starter ($50)",
                investedAmount = 50.0,
                startDate = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000),
                maturityDate = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                actualProfitOrLoss = 10.20,
                status = com.example.data.model.InvestmentStatus.COMPLETED
            )
            dao.insertInvestment(starterInvestment)

            // Seed System Settings for receiving account
            dao.insertSetting(SystemSettingEntity("easypaisa_holder", "Forex Desk Official"))
            dao.insertSetting(SystemSettingEntity("easypaisa_number", "03135445331"))
            dao.insertSetting(SystemSettingEntity("bank_holder", "Forex Trading Management Ltd"))
            dao.insertSetting(SystemSettingEntity("bank_account", "PK89BAHL0001928374829102"))
            dao.insertSetting(SystemSettingEntity("bank_name", "Bank Al-Habib / Standard"))

            // Seed welcome notification
            dao.insertNotification(
                NotificationEntity(
                    userId = investorId,
                    title = "Welcome to Forex Pro Simulator",
                    message = "Your virtual account is active. Explore 7-day packages, paper deposits, and strategy metrics."
                )
            )
        }
    }
}
