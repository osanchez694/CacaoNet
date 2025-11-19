package me.oscarsanchez.cacaonet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CacaoNetApp()
        }
    }
}

@Composable
fun CacaoNetApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLogin = { userType ->
                            when (userType) {
                                UserType.OPERADOR ->
                                    navController.navigate(Screen.OperatorDashboard.route)
                                UserType.PRODUCTOR ->
                                    navController.navigate(Screen.ProducerDashboard.route)
                                UserType.COMPRADOR ->
                                    navController.navigate(Screen.BuyerDashboard.route)
                            }
                        }
                    )
                }

                composable(Screen.OperatorDashboard.route) {
                    OperatorDashboardScreen(
                        goToRegisterDelivery = {
                            navController.navigate(Screen.RegisterDelivery.route)
                        },
                        goToRegisterQuality = {
                            navController.navigate(Screen.RegisterQuality.route)
                        },
                        goToReports = {
                            navController.navigate(Screen.Reports.route)
                        },
                        goToInventory = {
                            navController.navigate(Screen.Inventory.route)
                        },
                        goToPayments = {
                            navController.navigate(Screen.Payments.route)
                        },
                        goToOffline = {
                            navController.navigate(Screen.Offline.route)
                        }
                    )
                }

                composable(Screen.ProducerDashboard.route) {
                    ProducerDashboardScreen(
                        goToDeliveries = { /* TODO */ },
                        goToPayments = { /* TODO */ },
                        goToHistory = { /* TODO */ },
                        goToOffline = {
                            navController.navigate(Screen.Offline.route)
                        }
                    )
                }

                composable(Screen.BuyerDashboard.route) {
                    BuyerDashboardScreen(
                        goToLots = {
                            navController.navigate(Screen.Traceability.route)
                        },
                        goToOffline = {
                            navController.navigate(Screen.Offline.route)
                        }
                    )
                }

                composable(Screen.RegisterDelivery.route) {
                    RegisterDeliveryScreen(navController)
                }
                composable(Screen.RegisterQuality.route) {
                    RegisterQualityScreen(navController)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(navController)
                }
                composable(Screen.Traceability.route) {
                    TraceabilityScreen(navController)
                }
                composable(Screen.Offline.route) {
                    OfflineScreen(navController)
                }

                // 👇 nuevas pantallas
                composable(Screen.Inventory.route) {
                    InventoryScreen(navController)
                }
                composable(Screen.Payments.route) {
                    PaymentsScreen(navController)
                }
            }
        }
    }
}
