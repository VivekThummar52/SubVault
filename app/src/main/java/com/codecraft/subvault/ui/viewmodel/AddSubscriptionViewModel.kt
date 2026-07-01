package com.codecraft.subvault.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var category by mutableStateOf("Entertainment")
    var amount by mutableStateOf("")
    var currency by mutableStateOf("")
    var cycle by mutableStateOf("Monthly")
    var renewalDate by mutableLongStateOf(System.currentTimeMillis())
    var endDate by mutableStateOf<Long?>(null)
    var hasEndDate by mutableStateOf(false)
    var paymentMethod by mutableStateOf("Credit Card")

    private var isCurrencyInitialized = false

    init {
        viewModelScope.launch {
            preferenceRepository.getUserPreferences().collectLatest { prefs ->
                if (!isCurrencyInitialized) {
                    currency = prefs.defaultCurrency
                    if (prefs.defaultCurrency.isNotEmpty()) {
                        isCurrencyInitialized = true
                    }
                }
            }
        }
    }

    val defaultCurrency: StateFlow<String> = preferenceRepository.getUserPreferences()
        .map { it.defaultCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    fun addSubscription() {
        viewModelScope.launch {
            val subscription = Subscription(
                name = name,
                category = category,
                amount = amount.toDoubleOrNull() ?: 0.0,
                currency = currency,
                cycle = cycle,
                renewalDate = renewalDate,
                endDate = if (hasEndDate) endDate else null,
                paymentMethod = paymentMethod,
                isActive = true,
                iconUrl = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertSubscription(subscription)
            resetState()
        }
    }

    private fun resetState() {
        name = ""
        category = "Entertainment"
        amount = ""
        // Keep currency as the user might want to add multiple with same currency
        cycle = "Monthly"
        renewalDate = System.currentTimeMillis()
        endDate = null
        hasEndDate = false
        paymentMethod = "Credit Card"
    }
}
