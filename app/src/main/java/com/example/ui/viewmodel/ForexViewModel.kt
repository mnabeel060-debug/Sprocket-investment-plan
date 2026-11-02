package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DepositRequestEntity
import com.example.data.model.InvestmentPackageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.RequestStatus
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserInvestmentEntity
import com.example.data.model.UserRole
import com.example.data.model.WithdrawalRequestEntity
import com.example.data.repository.ForexRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ForexTicker(
    val pair: String,
    val bid: Double,
    val ask: Double,
    val changePercent: Double
)

class ForexViewModel(private val repository: ForexRepository) : ViewModel() {

    private val _currentUserId = MutableStateFlow<Long?>(2L) // Default to Demo Investor (id=2)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeUser(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val packages: StateFlow<List<InvestmentPackageEntity>> = repository.observePackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userInvestments: StateFlow<List<UserInvestmentEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeInvestments(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits: StateFlow<List<DepositRequestEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeDeposits(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeWithdrawals(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeTransactions(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.observeNotifications(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allUsers: StateFlow<List<UserEntity>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositRequestEntity>> = repository.observeAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = repository.observeAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvestments: StateFlow<List<UserInvestmentEntity>> = repository.observeAllInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsMap: StateFlow<Map<String, String>> = repository.observeSettings()
        .map { list -> list.associate { it.key to it.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Simulated Market Tickers
    private val _marketTickers = MutableStateFlow(
        listOf(
            ForexTicker("EUR/USD", 1.0845, 1.0847, +0.32),
            ForexTicker("GBP/USD", 1.2980, 1.2982, +0.15),
            ForexTicker("USD/JPY", 153.40, 153.42, -0.41),
            ForexTicker("XAU/USD", 2735.50, 2736.00, +1.18)
        )
    )
    val marketTickers: StateFlow<List<ForexTicker>> = _marketTickers.asStateFlow()

    init {
        startTickerSimulation()
    }

    private fun startTickerSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3500)
                _marketTickers.value = _marketTickers.value.map { ticker ->
                    val jitter = (Random.nextDouble() - 0.5) * 0.0008
                    val newBid = (ticker.bid + jitter).coerceAtLeast(0.1)
                    val newAsk = newBid + (ticker.ask - ticker.bid)
                    val newChange = (ticker.changePercent + (Random.nextDouble() - 0.5) * 0.05)
                    ticker.copy(bid = newBid, ask = newAsk, changePercent = newChange)
                }
            }
        }
    }

    fun clearMessages() {
        _authError.value = null
        _operationMessage.value = null
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.login(email, pass)
            result.onSuccess {
                _currentUserId.value = it.id
                _operationMessage.value = "Logged in as ${it.fullName}"
            }.onFailure {
                _authError.value = it.message ?: "Login failed"
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            if (name.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
                _authError.value = "All fields are required"
                return@launch
            }
            val result = repository.registerUser(name, email, phone, pass)
            result.onSuccess {
                _currentUserId.value = it.id
                _operationMessage.value = "Registration successful! Investor ID: ${it.investorId}"
            }.onFailure {
                _authError.value = it.message ?: "Registration failed"
            }
        }
    }

    fun quickSwitchUser(role: UserRole) {
        viewModelScope.launch {
            if (role == UserRole.ADMIN) {
                _currentUserId.value = 1L
                _operationMessage.value = "Switched to Administrator Workspace"
            } else {
                _currentUserId.value = 2L
                _operationMessage.value = "Switched to Demo Investor Profile"
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        _operationMessage.value = "Signed out"
    }

    fun activatePackage(pkg: InvestmentPackageEntity) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.activatePackageFromBalance(uid, pkg)
            res.onSuccess {
                _operationMessage.value = "${pkg.name} package successfully activated!"
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to activate package"
            }
        }
    }

    fun submitDeposit(
        amount: Double,
        pkgName: String,
        method: PaymentMethod,
        senderNumber: String,
        txnId: String,
        receiptNote: String
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.submitDepositRequest(uid, amount, pkgName, method, senderNumber, txnId, receiptNote)
            res.onSuccess {
                _operationMessage.value = "Deposit request submitted! Awaiting Admin verification."
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to submit deposit"
            }
        }
    }

    fun submitWithdrawal(
        amount: Double,
        method: PaymentMethod,
        accountHolder: String,
        accountNumber: String
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.submitWithdrawalRequest(uid, amount, method, accountHolder, accountNumber)
            res.onSuccess {
                _operationMessage.value = "Withdrawal request submitted for review."
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to submit withdrawal"
            }
        }
    }

    // Admin Actions
    fun adminApproveDeposit(depositId: Long) {
        viewModelScope.launch {
            val res = repository.approveDeposit(depositId)
            res.onSuccess {
                _operationMessage.value = "Deposit approved and credited to investor balance."
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to approve deposit"
            }
        }
    }

    fun adminRejectDeposit(depositId: Long) {
        viewModelScope.launch {
            val res = repository.rejectDeposit(depositId)
            res.onSuccess {
                _operationMessage.value = "Deposit request rejected."
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to reject deposit"
            }
        }
    }

    fun adminUpdateWithdrawalStatus(withdrawalId: Long, status: RequestStatus) {
        viewModelScope.launch {
            val res = repository.updateWithdrawalStatus(withdrawalId, status)
            res.onSuccess {
                _operationMessage.value = "Withdrawal status updated to $status."
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to update withdrawal"
            }
        }
    }

    fun adminDistributeProfitLoss(percent: Double, note: String) {
        viewModelScope.launch {
            val res = repository.distributeTradingProfitLoss(percent, note)
            res.onSuccess { count ->
                _operationMessage.value = "Applied ${if (percent >= 0) "+$percent%" else "$percent%"} to $count active investments!"
            }.onFailure {
                _operationMessage.value = it.message ?: "Failed to distribute returns"
            }
        }
    }

    fun adminSaveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            _operationMessage.value = "Settings updated successfully."
        }
    }

    fun markNotificationsAsRead() {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.markNotificationsRead(uid)
        }
    }
}

class ForexViewModelFactory(
    private val repository: ForexRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForexViewModel(repository) as T
    }
}
