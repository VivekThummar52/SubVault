package com.codecraft.subvault.domain.model

data class SpendingSummary(
    val totalMonthlySpend: Double,
    val activeCount: Int,
    val upcomingRenewals: List<SubscriptionRenewal>
)

data class SubscriptionRenewal(
    val id: Long,
    val name: String,
    val nextRenewalDate: Long,
    val amount: Double,
    val currency: String
)
