package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDashboardScreen(
    goToLots: () -> Unit,
    goToOffline: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Comprador") },
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
            Button(
                onClick = goToLots,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lotes disponibles / Trazabilidad")
            }
        }
    }
}
