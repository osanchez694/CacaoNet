package me.oscarsanchez.cacaonet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.oscarsanchez.cacaonet.ui.theme.CacaoNetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CacaoNetTheme {
                // 1. Monitor de Red
                NetworkMonitorInit()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 2. Mapa de Navegación
                    NavHost(
                        navController = navController,
                        startDestination = "operator_dashboard"
                    ) {

                        // --- DASHBOARD ---
                        composable("operator_dashboard") {
                            OperatorDashboardScreen(navController = navController)
                        }

                        // --- LOGIN ---
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLogin = {
                                    navController.navigate("operator_dashboard") {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- PANTALLAS DEL OPERADOR ---
                        // Ahora pasamos (navController) a todas, porque tu PaymentsScreen lo pide.

                        composable(Screen.Reports.route) {
                            ReportsScreen(navController)
                        }

                        composable(Screen.Inventory.route) {
                            InventoryScreen(navController)
                        }

                        composable(Screen.Payments.route) {
                            PaymentsScreen(navController)
                        }

                        composable(Screen.Producers.route) {
                            ProducersScreen(navController)
                        }
                    }
                }
            }
        }
    }
}