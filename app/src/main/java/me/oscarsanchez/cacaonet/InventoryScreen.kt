package me.oscarsanchez.cacaonet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// ======================================
//  INVENTARIO (Análisis Completado)
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var allDeliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var filteredDeliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var producersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    // ---------- Cargar nombres de productores ----------
    fun loadProducersMap(onComplete: () -> Unit) {
        db.collection("producers").get()
            .addOnSuccessListener { snapshot ->
                val temp = mutableMapOf<String, String>()
                snapshot.documents.forEach { doc ->
                    val name = doc.getString("producerName")
                        ?: doc.getString("nombre")
                        ?: doc.getString("name")
                        ?: "Desconocido"
                    temp[doc.id] = name
                }
                producersMap = temp
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    // ---------- Cargar entregas con status = "Análisis Completo" ----------
    fun loadInventoryDeliveries() {
        isLoading = true
        errorMessage = null

        loadProducersMap {
            db.collection("deliveries")
                .whereEqualTo("status", "Análisis Completo")
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val list = snapshot.documents.map { doc ->

                            fun tsToString(field: String): String? {
                                val v = doc.get(field)
                                return when (v) {
                                    is Timestamp -> {
                                        val date = v.toDate()
                                        SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
                                            .format(date)
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
                                deliveryDate = doc.get("deliveryDate"),
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
                        allDeliveries = list
                        isLoading = false
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.localizedMessage}"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.message ?: "Error al cargar"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) { loadInventoryDeliveries() }

    // ---------- Filtro por buscador ----------
    LaunchedEffect(searchText, allDeliveries, producersMap) {
        if (searchText.isBlank()) {
            filteredDeliveries = allDeliveries
        } else {
            val q = searchText.lowercase()

            filteredDeliveries = allDeliveries.filter { d ->
                val matchLote = d.lotId.lowercase().contains(q)
                val prodName = producersMap[d.producerId]?.lowercase() ?: ""
                val matchName = prodName.contains(q)

                val date = (d.deliveryDate as? Timestamp)?.toDate()
                val dateStr = if (date != null)
                    SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(date)
                else
                    d.analysisDate?.lowercase() ?: ""

                val matchDate = dateStr.contains(q)

                matchLote || matchName || matchDate
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {

                TopAppBar(
                    title = { Text("Inventario (Análisis Completado)") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { loadInventoryDeliveries() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                        }
                    }
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .height(50.dp),
                    placeholder = { Text("Buscar productor, fecha o lote...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F2FF),
                        unfocusedContainerColor = Color(0xFFF5F2FF),
                        disabledContainerColor = Color(0xFFF5F2FF),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->

        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                errorMessage != null -> Text(
                    text = errorMessage!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDeliveries) { delivery ->
                        InventoryCard(delivery)
                    }
                }
            }
        }
    }
}

// ====================================================================
//  TARJETA INVENTARIO – NUEVO CAMPO "PESO NETO"
// ====================================================================
@Composable
fun InventoryCard(delivery: Delivery) {

    val db = FirebaseFirestore.getInstance()
    var producerName by remember { mutableStateOf("") }

    LaunchedEffect(delivery.producerId) {
        try {
            val doc = db.collection("producers")
                .document(delivery.producerId)
                .get()
                .await()

            producerName = doc.getString("name")
                ?: doc.getString("producerName")
                        ?: delivery.producerId
        } catch (e: Exception) {
            producerName = delivery.producerId
        }
    }

    val dateString =
        (delivery.deliveryDate as? Timestamp)?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(it)
        } ?: delivery.analysisDate ?: "Sin fecha"

    // 👉 TOTAL PAGADO: se mantiene COMO ANTES (usa totalPayment de Firestore)
    val moneyFormat = String.format(
        Locale("es", "CO"),
        "$%,.0f",
        delivery.totalPayment ?: 0.0
    )

    // 👉 NUEVO: PESO NETO = pesoBruto * (1 - humedad/100)
    val pesoNeto = remember(delivery.weightKgBruto, delivery.moisturePercentage) {
        val bruto = delivery.weightKgBruto ?: 0.0
        val hum = delivery.moisturePercentage ?: 0.0
        bruto * (1 - hum / 100.0)
    }
    val pesoNetoTexto = String.format(Locale("es", "CO"), "%.1f", pesoNeto)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, null, tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(6.dp))
                    Text(dateString, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Text("COMPLETADO", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }

            // CUERPO
            Column(Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text("Productor", color = Color.Gray)
                        Text(producerName, fontWeight = FontWeight.Bold)
                        Text("Lote: ${delivery.lotId}", color = Color.Gray)
                    }

                    Spacer(Modifier.weight(1f))

                    Card(
                        modifier = Modifier.width(90.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Peso Bruto",
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${delivery.weightKgBruto ?: 0.0} kg",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Divider(Modifier.padding(vertical = 12.dp))

                Text("Datos de Calidad (Análisis)", color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = "${delivery.moisturePercentage ?: "N/A"} %",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Humedad") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = delivery.fermentationScore ?: "N/A",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Fermentación") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 🔹 NUEVA FILA: PESO NETO CALCULADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Peso Neto: $pesoNetoTexto kg",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = delivery.qualityGrade ?: "N/A",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Clasificación Final") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // FOOTER
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Precio por kg:", color = Color.Gray)
                    Text("$${String.format("%,.0f", delivery.pricePerKg ?: 0.0)} /kg")
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL PAGADO", fontWeight = FontWeight.Bold)
                    Text(
                        moneyFormat,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "Estado de Pago: ${delivery.paymentStatus ?: "N/A"}",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
