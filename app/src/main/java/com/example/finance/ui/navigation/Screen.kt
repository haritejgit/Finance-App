package com.example.finance.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ShiftSelection : Screen("shift_selection")
    object VillageList : Screen("village_list/{day}/{shift}") {
        fun createRoute(day: String, shift: String) = "village_list/$day/$shift"
    }
    object CustomerList : Screen("customer_list/{villageId}") {
        fun createRoute(villageId: String) = "customer_list/$villageId"
    }
    object CustomerProfile : Screen("customer_profile/{customerId}") {
        fun createRoute(customerId: String) = "customer_profile/$customerId"
    }
    object Reports : Screen("reports")
}
