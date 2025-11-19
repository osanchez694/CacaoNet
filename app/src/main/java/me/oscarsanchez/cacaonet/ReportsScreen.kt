package me.oscarsanchez.cacaonet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// =======================
//  MODELO DE LA ENTREGA
// =======================
data class Delivery(
    val id: String = "",

    // Bloque 1 – vendedor
    val producerId: String = "",
    val lotId: String = "",
    val weightKgBruto: Double? = null,
    val deliveryDate: String? = null,
    val status: String? = null, // <- Aquí se actualizará el estado

    // Bloque 2 – operador
    val operatorId: String? = null,
    val analysisDate: String? = null,
    val moisturePercentage: Double? = null,
    val fermentationScore: String? = null,
    val qualityGrade: String? = null,

    // Bloque 3 – cierre y pago
    val pricePerKg: Double? = null,
    val totalPayment: Double? = null,
    val paymentStatus: String? = null,
)

// =======================
//  PANTALLA DE REPORTES (ReportsScreen)
// =======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var deliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadDeliveries() {
        isLoading = true
        errorMessage = null

        db.collection("deliveries")
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val list = snapshot.documents.map { doc ->

                        fun tsToString(field: String): String? {
                            val v = doc.get(field)
                            return when (v) {
                                is Timestamp -> {
                                    val date = v.toDate()
                                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale("es", "ES"))
                                    formatter.format(date)
                                }
                                null -> null
                                else -> v.toString()
                            }
                        }

                        fun num(field: String): Double? {
                            val d = doc.getDouble(field)
                            if (d != null) return d
                            val l = doc.getLong(field)
                            return l?.toDouble()
                        }

                        fun anyToString(field: String): String? {
                            val v = doc.get(field)
                            return when (v) {
                                is String -> v
                                is Number -> v.toString()
                                null -> null
                                else -> v.toString()
                            }
                        }

                        Delivery(
                            id = doc.id,
                            producerId = doc.getString("producerId") ?: "",
                            lotId = doc.getString("lotId") ?: "",
                            weightKgBruto = num("weightKg_Bruto"),
                            deliveryDate = tsToString("deliveryDate"),
                            status = doc.getString("status"),

                            operatorId = doc.getString("operatorId"),
                            analysisDate = tsToString("analysisDate"),
                            moisturePercentage = num("moisturePercentage"),
                            fermentationScore = anyToString("fermentationScore"),
                            qualityGrade = doc.getString("qualityGrade"),

                            pricePerKg = num("pricePerKg"),
                            totalPayment = num("totalPayment"),
                            paymentStatus = doc.getString("paymentStatus"),
                        )
                    }
                    deliveries = list
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = "Error al leer datos: ${e.localizedMessage}"
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                errorMessage = e.message ?: "Error al cargar entregas"
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadDeliveries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes de entregas") },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(deliveries) { delivery ->
                            DeliveryEditableCard(
                                delivery = delivery,
                                onSave = { updated ->
                                    // --- Cálculos automáticos Bloque 3 ---
                                    val pricePerKg = when (updated.qualityGrade) {
                                        "Exportación" -> 12000.0
                                        "Nacional Estándar" -> 10000.0
                                        "Rechazo" -> 0.0
                                        else -> updated.pricePerKg
                                    }

                                    val netWeight = updated.weightKgBruto?.let { bruto ->
                                        val hum = updated.moisturePercentage ?: 0.0
                                        bruto * (1 - hum / 100.0)
                                    }

                                    val totalPayment = if (netWeight != null && pricePerKg != null) {
                                        netWeight * pricePerKg
                                    } else {
                                        updated.totalPayment
                                    }

                                    val paymentStatus =
                                        updated.paymentStatus ?: "Pendiente de Pago"

                                    // 1. **Actualizar el estado a 'Análisis Completo'**
                                    val newStatus = "Análisis Completo"

                                    db.collection("deliveries")
                                        .document(updated.id)
                                        .update(
                                            mapOf(
                                                // Bloque 1 - Actualización de estado
                                                "status" to newStatus, // <- ¡Aquí la corrección!

                                                // Bloque 2
                                                "operatorId" to updated.operatorId,
                                                "analysisDate" to Timestamp.now(),
                                                "moisturePercentage" to updated.moisturePercentage,
                                                "fermentationScore" to updated.fermentationScore,
                                                "qualityGrade" to updated.qualityGrade,

                                                // Bloque 3
                                                "pricePerKg" to pricePerKg,
                                                "totalPayment" to totalPayment,
                                                "paymentStatus" to paymentStatus,
                                            )
                                        )
                                        .addOnSuccessListener {
                                            loadDeliveries()
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================================
//  CARD EDITABLE PARA CADA ENTREGA (DeliveryEditableCard)
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryEditableCard(
    delivery: Delivery,
    onSave: (Delivery) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // 2. **Mostrar el nombre del productor (Lógica existente y comprobada)**
    var producerName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(delivery.producerId) {
        try {
            val doc = db.collection("producers")
                .document(delivery.producerId)
                .get()
                .await()
            // Obtener el campo 'name'. Si es nulo, usar el ID como fallback.
            producerName = doc.getString("name") ?: delivery.producerId
        } catch (e: Exception) {
            producerName = delivery.producerId
        }
    }

    // Bloque 2 – estados editables
    var moistureText by remember(delivery.id) {
        mutableStateOf(delivery.moisturePercentage?.toString() ?: "")
    }
    var fermentation by remember(delivery.id) {
        mutableStateOf(delivery.fermentationScore ?: "")
    }
    var quality by remember(delivery.id) {
        mutableStateOf(delivery.qualityGrade ?: "")
    }

    val qualityOptions = listOf("Exportación", "Nacional Estándar", "Rechazo")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ---------- Bloque 1 (solo lectura) ----------
            Text("Lote: ${delivery.lotId}")
            // Muestra el nombre del productor (Punto 2)
            Text("Productor: ${producerName ?: delivery.producerId}")
            Text("Peso bruto (kg): ${delivery.weightKgBruto ?: "-"}")
            Text("Fecha entrega: ${delivery.deliveryDate ?: "-"}")
            Text("Estado: ${delivery.status ?: "-"}")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // ---------- Bloque 2 (editable por operador) ----------
            OutlinedTextField(
                value = moistureText,
                onValueChange = { moistureText = it },
                label = { Text("Humedad (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fermentation,
                onValueChange = { fermentation = it },
                label = { Text("Nivel de fermentación") },
                modifier = Modifier.fillMaxWidth()
            )

            // Clasificación Final - Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = quality.ifEmpty { "Seleccione una opción" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clasificación Final") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    qualityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                quality = selectionOption
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // ---------- Bloque 3 (solo mostrado) ----------
            Text("Precio por kg: ${delivery.pricePerKg ?: "-"}")
            Text("Pago total: ${delivery.totalPayment ?: "-"}")
            Text("Estado de pago: ${delivery.paymentStatus ?: "-"}")

            Button(
                onClick = {
                    val updated = delivery.copy(
                        moisturePercentage = moistureText.toDoubleOrNull(),
                        fermentationScore = fermentation,
                        qualityGrade = quality,
                    )
                    onSave(updated)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar análisis")
            }
        }
    }
}