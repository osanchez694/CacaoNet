package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterQualityScreen(navController: NavController) {
    var humedad by remember { mutableStateOf("") }
    var fermentacion by remember { mutableStateOf("") }
    var clasificacion by remember { mutableStateOf("") }
    var etiquetaLote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de calidad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = humedad,
                onValueChange = { humedad = it },
                label = { Text("Humedad (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fermentacion,
                onValueChange = { fermentacion = it },
                label = { Text("Fermentación") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clasificacion,
                onValueChange = { clasificacion = it },
                label = { Text("Clasificación (fino, corriente, exportación)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = etiquetaLote,
                onValueChange = { etiquetaLote = it },
                label = { Text("Etiquetado del lote") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
