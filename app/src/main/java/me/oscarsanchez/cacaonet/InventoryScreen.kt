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
// ... (Otros imports)
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

// ... (El data class Delivery sigue siendo externo)

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

    // 🎯 NUEVA VARIABLE DE ESTADO PARA EL TOTAL
    var totalLoteValue by remember { mutableStateOf(0.0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    // ---------- Cargar nombres de productores ----------
    fun loadProducersMap(onComplete: () -> Unit) {
        db.collection("producers").get()
            .addOnSuccessListener { snapshot ->
                // ... (Lógica de carga de productores omitida)
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

    // ---------- Cargar entregas con status = "Análisis Completo" (LÓGICA ACTUALIZADA) ----------
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
                            // ... (Funciones auxiliares tsToString, num, anyToString omitidas)
                            fun tsToString(field: String): String? {
                                val v = doc.get(field)
                                return when (v) {
                                    is Timestamp -> {
                                        val date = v.toDate()
                                        SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(date)
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
                                weightKgNeto = num("weightKg_Neto"),
                            )
                        }
                        allDeliveries = list

                        // 🎯 CÁLCULO DEL TOTAL AL CARGAR LOS DATOS
                        totalLoteValue = list.sumOf { it.totalPayment ?: 0.0 }

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

    // ---------- Filtro por buscador (LÓGICA ACTUALIZADA) ----------
    LaunchedEffect(searchText, allDeliveries, producersMap) {
        if (searchText.isBlank()) {
            filteredDeliveries = allDeliveries
        } else {
            // ... (Lógica de filtrado omitida por brevedad)
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

                val grade = d.qualityGrade?.lowercase() ?: ""
                val matchGrade = grade.contains(q)

                val matchExportacion = q.contains("exportacion") && grade.contains("exportacion")
                val matchNacional = q.contains("nacional") && grade.contains("nacional")

                matchLote || matchName || matchDate || matchGrade || matchExportacion || matchNacional
            }
        }
        // 🎯 CÁLCULO DEL TOTAL CON BASE EN LOS LOTES FILTRADOS
        totalLoteValue = filteredDeliveries.sumOf { it.totalPayment ?: 0.0 }
    }

    Scaffold(
        containerColor = Color(0xFFD7CCC8), // LatteBackground
        topBar = {
            // ... (TopBar con Título y Buscador omitido)
            Column(modifier = Modifier.background(Color(0xFF3E2723))) { // DeepChocolate

                CenterAlignedTopAppBar(
                    title = { Text("Inventario", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { loadInventoryDeliveries() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .heightIn(min = 48.dp),
                    placeholder = { Text("Buscar: Productor, Lote, Fecha, Exportación/Nacional...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.Gray)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFF8E1),
                        unfocusedContainerColor = Color(0xFFFFF8E1),
                        disabledContainerColor = Color(0xFFFFF8E1),
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
                .background(Color(0xFFD7CCC8))
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
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Ajustamos padding para la nueva tarjeta
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🎯 NUEVO COMPONENTE DE TARJETA TOTAL
                    item {
                        TotalValueCard(totalLoteValue)
                        Spacer(Modifier.height(8.dp)) // Espacio extra después del total
                    }

                    items(filteredDeliveries) { delivery ->
                        InventoryCardMinimal(delivery, db)
                    }
                }
            }
        }
    }
}

// ====================================================================
//  NUEVA TARJETA DE VALOR TOTAL DEL INVENTARIO
// ====================================================================
@Composable
fun TotalValueCard(totalValue: Double) {
    val moneyFormat = String.format(Locale("es", "CO"), "$%,.0f", totalValue)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp), // Altura mínima para que se vea como un botón grande
        colors = CardDefaults.cardColors(containerColor = Color(0xFF5D4037)), // MilkChocolate
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VALOR TOTAL DEL INVENTARIO",
                color = Color(0xFFD7CCC8), // LatteBackground (Claro)
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = moneyFormat,
                color = Color(0xFFFFCC80), // Naranja Claro / Ámbar para resaltar
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ====================================================================
//  TARJETA MINIMALISTA (SIN CAMBIOS EN LÓGICA INTERNA)
// ====================================================================
@Composable
fun InventoryCardMinimal(delivery: Delivery, db: FirebaseFirestore) {
    // ... (El resto de InventoryCardMinimal es el mismo que en tu código anterior)
    var producerName by remember { mutableStateOf("") }
    LaunchedEffect(delivery.producerId) {
        try {
            val doc = db.collection("producers").document(delivery.producerId).get().await()
            producerName = doc.getString("name") ?: doc.getString("producerName") ?: delivery.producerId
        } catch (e: Exception) {
            producerName = delivery.producerId
        }
    }

    val dateString = (delivery.deliveryDate as? Timestamp)?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(it)
    } ?: delivery.analysisDate ?: "Sin fecha"

    val moneyFormat = String.format(Locale("es", "CO"), "$%,.0f", delivery.totalPayment ?: 0.0)

    val pesoNeto = remember(delivery.weightKgNeto, delivery.weightKgBruto, delivery.moisturePercentage) {
        delivery.weightKgNeto ?: run {
            val bruto = delivery.weightKgBruto ?: 0.0
            val hum = delivery.moisturePercentage ?: 0.0
            bruto * (1 - hum / 100.0)
        }
    }
    val pesoNetoTexto = String.format(Locale("es", "CO"), "%.1f", pesoNeto)

    // --- Diseño Minimalista ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Fondo Crema
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(Modifier.padding(16.dp)) {

            // 1. HEADER (Fecha y Estado - Simplificado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        dateString,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                // Estado (Verde Intenso para el POP minimalista)
                Text(
                    "COMPLETADO",
                    color = Color(0xFF1B5E20), // Verde más oscuro
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(10.dp))

            // 2. PRODUCTOR Y LOTE (Principal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        producerName,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3E2723) // Marrón oscuro
                    )
                    Text(
                        "Lote: ${delivery.lotId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // 3. PESO BRUTO (Simplificado a un texto)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Bruto:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${delivery.weightKgBruto ?: 0.0} kg",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF5D4037).copy(alpha = 0.2f))

            // 4. DATOS DE CALIDAD (Minimalista - Sin OutlinedTextFields)
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Análisis de Calidad",
                    color = Color(0xFF3E2723),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Humedad
                    Column {
                        Text("Humedad:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text("${delivery.moisturePercentage ?: "N/A"} %", fontWeight = FontWeight.Medium)
                    }
                    // Fermentación
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Fermentación:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(delivery.fermentationScore ?: "N/A", fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Clasificación Final (Alineado a la izquierda para claridad)
                Text("Clasificación Final:", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                Text(delivery.qualityGrade ?: "N/A", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            }

            Spacer(Modifier.height(12.dp))

            // 5. FOOTER (Pago y Peso Neto)
            // Peso Neto arriba del total para mejor flujo visual
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Peso Neto",
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    "$pesoNetoTexto kg",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3E2723)
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Color(0xFF5D4037).copy(alpha = 0.1f)) // Divisor más sutil
            Spacer(Modifier.height(8.dp))

            // Precio del Lote y Estado
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PRECIO DEL LOTE", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    Text(
                        "Estado: ${delivery.paymentStatus ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    moneyFormat,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE65100) // Naranja para el total
                )
            }
        }
    }
}