package com.codecraft.subvault.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecraft.subvault.domain.model.PriceChangeLog
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val preferenceRepository: PreferenceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subscriptionId: Long = checkNotNull(savedStateHandle["subscriptionId"])

    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()

    val priceHistory: StateFlow<List<PriceChangeLog>> = repository.getPriceHistory(subscriptionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultCurrency: StateFlow<String> = preferenceRepository.getUserPreferences()
        .map { it.defaultCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    init {
        loadSubscription()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            _subscription.value = repository.getSubscriptionById(subscriptionId)
        }
    }

    fun updateSubscription(
        name: String,
        amount: Double,
        currency: String,
        cycle: String,
        category: String,
        renewalDate: Long,
        endDate: Long?,
        paymentMethod: String
    ) {
        val current = _subscription.value ?: return
        viewModelScope.launch {
            repository.updateSubscription(
                current.copy(
                    name = name,
                    amount = amount,
                    currency = currency,
                    cycle = cycle,
                    category = category,
                    renewalDate = renewalDate,
                    endDate = endDate,
                    paymentMethod = paymentMethod,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
