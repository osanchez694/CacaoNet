package me.oscarsanchez.cacaonet

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDeliveryScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Estados de los campos
    var productorNombre by remember { mutableStateOf("") }
    var loteCodigo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var humedad by remember { mutableStateOf("") }
    var tipoCacao by remember { mutableStateOf("") }

    // Estado para controlar el botón de carga
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Entrega") },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Ingrese los datos de la recepción:",
                style = MaterialTheme.typography.titleMedium
            )

            // 1. Nombre del Productor
            OutlinedTextField(
                value = productorNombre,
                onValueChange = { productorNombre = it },
                label = { Text("Nombre del productor") },
                placeholder = { Text("Ej. Juan Pérez") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Código de Lote (Necesario para el reporte)
            OutlinedTextField(
                value = loteCodigo,
                onValueChange = { loteCodigo = it },
                label = { Text("Código de Lote") },
                placeholder = { Text("Ej. LOT-2024-001") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Peso (Numérico)
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso Bruto (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // 4. Humedad (Numérico)
            OutlinedTextField(
                value = humedad,
                onValueChange = { humedad = it },
                label = { Text("Humedad preliminar (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // 5. Tipo de Cacao (Opcional, va a QualityGrade o notas)
            OutlinedTextField(
                value = tipoCacao,
                onValueChange = { tipoCacao = it },
                label = { Text("Variedad / Tipo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = !isSaving, // Deshabilitar si está guardando
                onClick = {
                    // Validaciones básicas
                    if (productorNombre.isEmpty() || peso.isEmpty()) {
                        Toast.makeText(context, "El nombre y el peso son obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true

                    // --- MAPEO DE DATOS PARA QUE COINCIDA CON REPORTSSCREEN ---
                    val deliveryData = hashMapOf(
                        // Guardamos el NOMBRE escrito en el campo ID.
                        // Así el reporte mostrará el nombre directamente sin buscar códigos.
                        "producerId" to productorNombre,

                        "lotId" to loteCodigo.ifEmpty { "SIN-LOTE-${System.currentTimeMillis()}" },
                        "weightKg_Bruto" to (peso.toDoubleOrNull() ?: 0.0),
                        "moisturePercentage" to (humedad.toDoubleOrNull() ?: 0.0),

                        // Usamos Timestamp de Firebase para la fecha actual automática
                        "deliveryDate" to Timestamp.now(),

                        "status" to "Recibido",
                        "qualityGrade" to tipoCacao, // Guardamos el tipo aquí inicialmente

                        // Campos vacíos necesarios para evitar nulos
                        "fermentationScore" to "",
                        "paymentStatus" to "Pendiente",
                        "pricePerKg" to 0.0,
                        "totalPayment" to 0.0
                    )

                    // Guardamos en la colección CORRECTA ("deliveries")
                    db.collection("deliveries")
                        .add(deliveryData)
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Entrega registrada con éxito", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrar Entrega")
                }
            }
        }
    }
}