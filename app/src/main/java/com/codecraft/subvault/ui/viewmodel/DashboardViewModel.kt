package com.codecraft.subvault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecraft.subvault.domain.model.SpendingSummary
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val dashboardSummary: StateFlow<SpendingSummary?> = repository.getDashboardSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subscriptions: StateFlow<List<Subscription>> = repository.getAllSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultCurrency: StateFlow<String> = preferenceRepository.getUserPreferences()
        .map { it.defaultCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    fun toggleSubscriptionActive(subscription: Subscription) {
        viewModelScope.launch {
            repository.updateSubscription(subscription.copy(isActive = !subscription.isActive))
        }
    }

    fun setDefaultCurrency(currency: String) {
        viewModelScope.launch {
            preferenceRepository.saveDefaultCurrency(currency)
        }
    }
}
