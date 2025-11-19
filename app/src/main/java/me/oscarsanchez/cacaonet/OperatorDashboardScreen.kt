package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    goToRegisterDelivery: () -> Unit,
    goToRegisterQuality: () -> Unit,
    goToReports: () -> Unit,
    goToInventory: () -> Unit,
    goToPayments: () -> Unit,
    goToOffline: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Operador") },
                actions = {
                    TextButton(onClick = goToOffline) {
                        Text("Offline")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = goToRegisterDelivery, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar entrega de cacao")
            }
            Button(onClick = goToRegisterQuality, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar calidad")
            }
            Button(onClick = goToReports, modifier = Modifier.fillMaxWidth()) {
                Text("Reportes")
            }
            Button(onClick = goToInventory, modifier = Modifier.fillMaxWidth()) {
                Text("Inventario")
            }
            Button(onClick = goToPayments, modifier = Modifier.fillMaxWidth()) {
                Text("Pagos a productores")
            }
        }
    }
}
