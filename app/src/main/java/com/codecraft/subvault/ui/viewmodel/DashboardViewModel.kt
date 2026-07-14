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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus = _selectedStatus.asStateFlow()

    val dashboardSummary: StateFlow<SpendingSummary?> = repository.getDashboardSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allSubscriptions = repository.getAllSubscriptions()

    val categories: StateFlow<List<String>> = allSubscriptions
        .map { subs -> listOf("All") + subs.map { it.category }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val subscriptions: StateFlow<List<Subscription>> = combine(
        allSubscriptions, 
        _searchQuery, 
        _selectedCategory, 
        _selectedStatus
    ) { subs, query, category, status ->
        val now = System.currentTimeMillis()
        subs.map { sub ->
            val isExpired = sub.endDate != null && sub.endDate < now
            if (sub.isActive && isExpired) sub.copy(isActive = false) else sub
        }.filter { sub ->
            val matchesSearch = query.isBlank() || sub.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || sub.category == category
            val matchesStatus = when (status) {
                "Active" -> sub.isActive
                "Expired" -> !sub.isActive
                else -> true
            }
            matchesSearch && matchesCategory && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultCurrency: StateFlow<String> = preferenceRepository.getUserPreferences()
        .map { it.defaultCurrency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun onStatusSelect(status: String) {
        _selectedStatus.value = status
    }

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
