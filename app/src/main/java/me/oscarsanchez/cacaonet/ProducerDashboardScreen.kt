package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerDashboardScreen(
    goToDeliveries: () -> Unit,
    goToPayments: () -> Unit,
    goToHistory: () -> Unit,
    goToOffline: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Productor") },
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
            Button(onClick = goToDeliveries, modifier = Modifier.fillMaxWidth()) {
                Text("Mis entregas")
            }
            Button(onClick = goToPayments, modifier = Modifier.fillMaxWidth()) {
                Text("Pagos recibidos")
            }
            Button(onClick = goToHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Mi historial")
            }
        }
    }
}
