package com.example.finance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finance.auth.AuthManager
import com.example.finance.ui.navigation.Screen
import com.example.finance.ui.screens.auth.AuthViewModel
import com.example.finance.ui.screens.auth.LoginScreen
import com.example.finance.ui.screens.customer.CustomerListScreen
import com.example.finance.ui.screens.customer.CustomerProfileScreen
import com.example.finance.ui.screens.reports.ReportsScreen
import com.example.finance.ui.screens.shift.ShiftSelectionScreen
import com.example.finance.ui.screens.village.VillageListScreen
import com.example.finance.ui.theme.FinanceTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authManager: AuthManager
    
    private val viewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Email Sign-in link if present
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            FinanceTheme {
                FinanceNavHost(authManager = authManager)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val emailLink = intent?.data?.toString()
        if (emailLink != null && authManager.getCurrentUser() == null) {
            // Here we would ideally have a way to get the email from storage or user input
            // For now, we assume the user is still in the process of signing in
        }
    }
}

@Composable
fun FinanceNavHost(authManager: AuthManager) {
    val navController = rememberNavController()
    val startDestination = if (authManager.isUserSignedIn()) Screen.ShiftSelection.route else Screen.Login.route
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ShiftSelection.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ShiftSelection.route) {
            ShiftSelectionScreen(
                onNavigateToVillages = { day, shift ->
                    navController.navigate(Screen.VillageList.createRoute(day, shift))
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.VillageList.route,
            arguments = listOf(
                navArgument("day") { type = NavType.StringType },
                navArgument("shift") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: ""
            val shift = backStackEntry.arguments?.getString("shift") ?: ""
            VillageListScreen(
                day = day,
                shift = shift,
                onNavigateToCustomers = { villageId ->
                    navController.navigate(Screen.CustomerList.createRoute(villageId))
                }
            )
        }
        
        composable(
            route = Screen.CustomerList.route,
            arguments = listOf(navArgument("villageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val villageId = backStackEntry.arguments?.getString("villageId") ?: ""
            CustomerListScreen(
                villageId = villageId,
                onNavigateToProfile = { customerId ->
                    navController.navigate(Screen.CustomerProfile.createRoute(customerId))
                }
            )
        }
        
        composable(
            route = Screen.CustomerProfile.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            CustomerProfileScreen(customerId = customerId)
        }

        composable(Screen.Reports.route) {
            ReportsScreen()
        }
    }
}
