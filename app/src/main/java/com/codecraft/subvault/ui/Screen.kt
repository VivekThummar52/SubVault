package com.codecraft.subvault.ui

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Dashboard : Screen("dashboard")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object Account : Screen("account")
    object AddSubscription : Screen("add_subscription")
    object EditSubscription : Screen("edit_subscription/{subscriptionId}") {
        fun createRoute(id: Long) = "edit_subscription/$id"
    }
}
