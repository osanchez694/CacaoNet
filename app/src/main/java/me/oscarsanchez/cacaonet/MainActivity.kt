package me.oscarsanchez.cacaonet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

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
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // 🔥 Usuario actual de Firebase
    val currentUser = auth.currentUser
    val savedType = SessionManager.getUserType(context)

    // 🔥 Si no hay sesión o no guardamos tipo → Login
    // 🔥 Si hay sesión y tipo guardado → va directo al dashboard correcto
    val startDestination = when {
        currentUser == null || savedType == null -> Screen.Login.route
        savedType == UserType.OPERADOR -> Screen.OperatorDashboard.route
        savedType == UserType.PRODUCTOR -> Screen.ProducerDashboard.route
        savedType == UserType.COMPRADOR -> Screen.BuyerDashboard.route
        else -> Screen.Login.route
    }

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // LOGIN
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLogin = { userType ->
                            // 🧠 guardamos el tipo de usuario en preferencias
                            SessionManager.saveUserType(context, userType)

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

                // 2) DASHBOARDS
                composable(Screen.OperatorDashboard.route) {
                    OperatorDashboardScreen(navController)
                }

                composable(Screen.ProducerDashboard.route) {
                    ProducerDashboardScreen(navController)
                }

                composable(Screen.BuyerDashboard.route) {
                    BuyerDashboardScreen(navController)
                }

                // 3) PANTALLAS OPERADOR
                composable(Screen.RegisterDelivery.route) {
                    RegisterDeliveryScreen(navController)
                }
                composable(Screen.RegisterQuality.route) {
                    RegisterQualityScreen(navController)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(navController)
                }
                composable(Screen.Inventory.route) {
                    InventoryScreen(navController)
                }
                composable(Screen.Payments.route) {
                    PaymentsScreen(navController)
                }

                // 4) TRAZABILIDAD / OFFLINE / PRODUCTORES
                composable(Screen.Traceability.route) {
                    TraceabilityScreen(navController)
                }
                composable(Screen.Offline.route) {
                    OfflineScreen(navController)
                }
                composable(Screen.Producers.route) {
                    ProducersScreen(navController)
                }
            }
        }
    }
}
