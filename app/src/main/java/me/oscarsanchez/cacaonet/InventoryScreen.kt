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

// ======================================
//  PANTALLA DE INVENTARIO (NUEVA)
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var deliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadInventoryDeliveries() { // Función de carga con filtros
        isLoading = true
        errorMessage = null

        db.collection("deliveries")
            // 1. FILTRO REQUERIDO: status == "Análisis Completo"
            .whereEqualTo("status", "Análisis Completo")

            // 2. FILTRO ADICIONAL DE EJEMPLO: Solo Pendientes de Pago
            .whereEqualTo("paymentStatus", "Pendiente de Pago") // <-- Filtro añadido aquí

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
                errorMessage = e.message ?: "Error al cargar entregas de inventario"
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadInventoryDeliveries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario (Análisis Completado)") },
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
                            DeliveryReadOnlyCard(delivery = delivery)
                        }
                    }
                }
            }
        }
    }
}

// ======================================
//  CARD DE SOLO LECTURA PARA INVENTARIO (Con formato de Pago)
// ======================================
@Composable
fun DeliveryReadOnlyCard(
    delivery: Delivery
) {
    val db = FirebaseFirestore.getInstance()
    var producerName by remember { mutableStateOf<String?>(null) }

    // Lógica para obtener el nombre del productor
    LaunchedEffect(delivery.producerId) {
        try {
            val doc = db.collection("producers")
                .document(delivery.producerId)
                .get()
                .await()
            producerName = doc.getString("name") ?: delivery.producerId
        } catch (e: Exception) {
            producerName = delivery.producerId
        }
    }

    // 3. Formatear el pago total a dos decimales y formato local
    val formattedTotalPayment = delivery.totalPayment?.let { payment ->
        // Esto corrige el problema de los decimales largos
        String.format(Locale("es", "ES"), "%,.2f", payment)
    }

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
            Text("Lote: ${delivery.lotId}", style = MaterialTheme.typography.titleMedium)
            // 1. Muestra el nombre cargado, no el ID
            Text("Productor: ${producerName ?: delivery.producerId}")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text("Estado: ${delivery.status ?: "N/A"}", color = MaterialTheme.colorScheme.primary)
            Text("Clasificación: ${delivery.qualityGrade ?: "-"}")
            Text("Peso bruto (kg): ${delivery.weightKgBruto ?: "-"}")
            Text("Humedad (%): ${delivery.moisturePercentage ?: "-"}")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // 3. Muestra el pago con formato
            Text("Pago total: $${formattedTotalPayment ?: "-"}", style = MaterialTheme.typography.titleSmall)
            Text("Estado de pago: ${delivery.paymentStatus ?: "-"}")
        }
    }
}