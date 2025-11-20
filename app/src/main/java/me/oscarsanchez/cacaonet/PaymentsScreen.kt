package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pagos y Ventas") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Opciones de Transacciones:",
                style = MaterialTheme.typography.titleLarge
            )

            // --- BOTÓN PARA REGISTRAR VENTA ---
            Button(
                onClick = {
                    // ** IMPORTANTE: Asegúrate de que tu NavGraph use el destino "registerSale" **
                    navController.navigate("registerSale")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text("Registrar Nueva Venta (Entrega a Comprador)")
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // --- LÓGICA DE PAGOS EXISTENTE ---
            Text(
                "Pagos a Productores:",
                style = MaterialTheme.typography.titleMedium
            )
            // Aquí iría tu lógica actual de la pantalla de Pagos (listas, etc.)
            Text("Lista o resumen de pagos pendientes/realizados...")
        }
    }
}