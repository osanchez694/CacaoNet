package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ======================================
//  PANTALLA DE GESTIÓN DE PAGOS Y VENTAS
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavController) {

    // Color(0xFFD7CCC8) es LatteBackground
    Scaffold(
        containerColor = Color(0xFFD7CCC8),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Pagos y Ventas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                // ⬅️ Botón de retroceso (Flecha para devolverse)
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3E2723) // Color(0xFF3E2723) es DeepChocolate
                )
            )
        }
    ) { paddingValues ->

        // Color(0xFF3E2723) es DeepChocolate
        val deepChocolateColor = Color(0xFF3E2723)
        // Color(0xFFC5CAE9) es LightBlue
        val lightBlueColor = Color(0xFFC5CAE9)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Sección 1: Opciones de Transacciones
            Text(
                text = "Opciones de Transacciones:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = deepChocolateColor
            )

            // Botón: Registrar Nueva Venta (Entrega a Comprador)
            Button(
                onClick = {
                    navController.navigate("registerSale")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightBlueColor, // Fondo azul claro
                    contentColor = deepChocolateColor // Texto en color oscuro
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Registrar Nueva Venta (Entrega a Comprador)",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // El divisor usa el color DeepChocolate con una transparencia de 30% (alpha = 0.3f)
            Divider(color = deepChocolateColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Sección 2: Pagos a Productores
            Text(
                text = "Pagos a Productores:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = deepChocolateColor
            )

            // Placeholder/resumen actual
            Text(
                text = "Lista o resumen de pagos pendientes/realizados...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // ...
        }
    }
}