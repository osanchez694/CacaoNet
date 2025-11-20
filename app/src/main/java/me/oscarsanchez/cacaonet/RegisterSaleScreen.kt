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
import kotlinx.coroutines.tasks.await

// --- Data class para el Lote (usada en el selector) ---
data class LotData(
    val lotId: String,
    val producerId: String,
    val weightKg: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSaleScreen(navController: NavController) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Estados de los campos de la Venta
    var buyerName by remember { mutableStateOf("") }
    var pricePerKg by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Estados para la selección del Lote
    var selectedLot by remember { mutableStateOf<LotData?>(null) }
    var lotesDisponibles by remember { mutableStateOf(listOf<LotData>()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isLoadingLots by remember { mutableStateOf(true) }

    // Cargar lotes al iniciar
    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("deliveries")
                .whereEqualTo("status", "Recibido") // Filtra lotes listos para vender
                .get().await()

            lotesDisponibles = snapshot.documents.map { doc ->
                LotData(
                    lotId = doc.getString("lotId") ?: "N/A",
                    producerId = doc.getString("producerId") ?: "N/A",
                    weightKg = doc.getDouble("weightKg_Bruto") ?: 0.0
                )
            }.filter { it.lotId != "N/A" } // Asegura que el lote tenga un ID válido
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar lotes: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoadingLots = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Venta") },
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
            // Sección de Selección de Lote
            Text("1. Seleccionar Lote:", style = MaterialTheme.typography.titleMedium)

            if (isLoadingLots) {
                CircularProgressIndicator(Modifier.size(24.dp))
                Text("Cargando lotes disponibles...")
            } else if (lotesDisponibles.isEmpty()) {
                Text("No hay lotes disponibles para vender.")
            } else {
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLot?.lotId ?: "Seleccione un Lote",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Código de Lote *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        lotesDisponibles.forEach { lot ->
                            DropdownMenuItem(
                                text = { Text("${lot.lotId} (Peso: ${lot.weightKg} kg)") },
                                onClick = {
                                    selectedLot = lot
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mostrar datos del lote seleccionado (Requisitos del audio)
                selectedLot?.let { lot ->
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Detalles del Lote:")
                    Text("- **Productor:** ${lot.producerId}", style = MaterialTheme.typography.bodyLarge)
                    Text("- **Peso (kg):** ${lot.weightKg}", style = MaterialTheme.typography.bodyLarge)
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Datos de la Venta
            Text("2. Datos de la Venta:", style = MaterialTheme.typography.titleMedium)

            // 1. A quién se vendió (Comprador)
            OutlinedTextField(
                value = buyerName,
                onValueChange = { buyerName = it },
                label = { Text("Nombre del Comprador *") },
                placeholder = { Text("Ej. Chocolates del Sur S.A.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Precio por Kg (Numérico)
            OutlinedTextField(
                value = pricePerKg,
                onValueChange = { pricePerKg = it },
                label = { Text("Precio de Venta por Kg *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            val totalPayment = (pricePerKg.toDoubleOrNull() ?: 0.0) * (selectedLot?.weightKg ?: 0.0)
            Text("Pago Total Estimado: $${String.format("%.2f", totalPayment)}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = !isSaving && selectedLot != null && buyerName.isNotEmpty() && pricePerKg.toDoubleOrNull() != null,
                onClick = {
                    isSaving = true

                    val lot = selectedLot!!
                    val saleData = hashMapOf(
                        "lotId" to lot.lotId,
                        "producerId" to lot.producerId, // Mantenemos el productor original
                        "buyerName" to buyerName, // Nuevo campo de Venta
                        "weightKg_Venta" to lot.weightKg, // Peso vendido
                        "pricePerKg_Venta" to (pricePerKg.toDoubleOrNull() ?: 0.0), // Precio de Venta
                        "totalSale" to totalPayment,
                        "saleDate" to Timestamp.now(), // Fecha de la Venta
                        "status" to "Vendido"
                    )

                    // 1. Guardar el registro de la Venta en una nueva colección "sales"
                    db.collection("sales")
                        .add(saleData)
                        .addOnSuccessListener {
                            // 2. Actualizar el estado del lote original en "deliveries" a "Vendido"
                            db.collection("deliveries").whereEqualTo("lotId", lot.lotId).get()
                                .addOnSuccessListener { querySnapshot ->
                                    querySnapshot.documents.forEach { doc ->
                                        doc.reference.update("status", "Vendido")
                                    }
                                    isSaving = false
                                    Toast.makeText(context, "Venta registrada y lote actualizado con éxito", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Venta registrada, pero falló al actualizar lote: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(context, "Error al registrar la venta: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrar Venta y Entrega")
                }
            }
        }
    }
}