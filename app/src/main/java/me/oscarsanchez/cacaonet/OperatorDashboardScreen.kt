package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Operador") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Primero intento volver atrás
                            val pudoVolver = navController.popBackStack()

                            // Si no había nada en el back stack, voy al Login
                            if (!pudoVolver) {
                                navController.navigate(Screen.Login.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate(Screen.Offline.route) }) {
                        Text("Offline")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ✅ Solo quedan los botones solicitados (Reportes, Inventario, Pagos, Productores)
            BigButton("Reportes") {
                navController.navigate(Screen.Reports.route)
            }
            BigButton("Inventario") {
                navController.navigate(Screen.Inventory.route)
            }
            BigButton("Pagos a productores") {
                navController.navigate(Screen.Payments.route)
            }
            BigButton("Productores") {
                navController.navigate(Screen.Producers.route)
            }
        }
    }
}

@Composable
fun BigButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(40.dp)
    ) {
        Text(text)
    }
}