package me.oscarsanchez.cacaonet

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineScreen(navController: NavController) {
    // AQUI LEEMOS EL ESTADO REAL DE LA RED
    val isOnline = AppState.isOnline.value
    val context = LocalContext.current

    // Colores (Manuales para evitar errores)
    val LatteBackground = Color(0xFFD7CCC8)
    val DeepChocolate = Color(0xFF3E2723)

    val statusColor = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
    val statusIcon = if (isOnline) Icons.Default.CheckCircle else Icons.Default.Warning
    val titleText = if (isOnline) "Conexión Activa" else "Sin Internet"

    val descriptionText = if (isOnline)
        "Tu dispositivo está conectado correctamente a la red."
    else
        "No se detecta conexión. Los datos se guardarán localmente hasta que recuperes la señal."

    Scaffold(
        containerColor = LatteBackground,
        topBar = {
            TopAppBar(
                title = { Text("Estado de Conexión") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LatteBackground),
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
                modifier = Modifier.size(140.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(80.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(titleText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = statusColor)

            Spacer(modifier = Modifier.height(16.dp))

            Text(descriptionText, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(48.dp))

            // BOTON REAL: ABRE LA CONFIGURACIÓN DEL CELULAR
            if (!isOnline) {
                Button(
                    onClick = {
                        // Esto abre el menú de WiFi del celular de verdad
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepChocolate),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Revisar Configuración WiFi")
                }
            }
        }
    }
}