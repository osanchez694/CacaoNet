package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerDashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Productor") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            auth.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 👉 Mis entregas
            Button(
                onClick = {
                    navController.navigate(Screen.Traceability.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mis entregas")
            }

            // 👉 Pagos recibidos
            Button(
                onClick = {
                    navController.navigate(Screen.Payments.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pagos recibidos")
            }

            // 👉 Mi historial
            Button(
                onClick = {
                    navController.navigate(Screen.Reports.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mi historial")
            }
        }
    }
}
