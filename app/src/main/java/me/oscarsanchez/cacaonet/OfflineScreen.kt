package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(navController: NavController) {
    // LEEMOS EL ESTADO GLOBAL AQUI
    val isOnline = AppState.isOnline.value

    val statusColor = if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val statusIcon = if (isOnline) Icons.Default.CheckCircle else Icons.Default.Warning
    val titleText = if (isOnline) "Estás en Línea" else "Modo Offline"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Conexión") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(64.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(titleText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = statusColor)

            Spacer(modifier = Modifier.height(48.dp))

            // BOTÓN QUE CAMBIA EL ESTADO GLOBAL
            Button(
                onClick = {
                    // AQUI OCURRE LA MAGIA: Cambiamos la variable global
                    AppState.isOnline.value = !AppState.isOnline.value
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isOnline) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (isOnline) "Simular Pérdida de Señal" else "Conectar a Internet")
            }
        }
    }
}