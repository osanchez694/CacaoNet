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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// ======================================
//  INVENTORY SCREEN COMPLETO
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

    // ---------- CARGAR NOMBRES DE PRODUCTORES ----------
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

    // ---------- CARGAR INVENTARIO (solo completados) ----------
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
                        errorMessage = "Error al leer datos: ${e.localizedMessage}"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.message ?: "Error al cargar inventario"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        loadInventoryDeliveries()
    }

    // ---------- FILTRADO BUSCADOR ----------
    LaunchedEffect(searchText, allDeliveries, producersMap) {
        if (searchText.isBlank()) {
            filteredDeliveries = allDeliveries
        } else {
            val q = searchText.lowercase()

            filteredDeliveries = allDeliveries.filter { d ->
                val matchLote = d.lotId.lowercase().contains(q)

                val prod = producersMap[d.producerId]?.lowercase() ?: ""
                val matchName = prod.contains(q)

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

    // ---------- UI ----------
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = { Text("Inventario (Análisis Completado)") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { loadInventoryDeliveries() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Recargar Inventario"
                            )
                        }
                    }
                )

                // ====== BARRA DE BÚSQUEDA ======
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .heightIn(min = 48.dp),
                    placeholder = { Text("Buscar productor, fecha o lote...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
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
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                errorMessage != null -> Text(
                    text = errorMessage!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )

                else -> {
                    LazyColumn(
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
}

// ========================================
//  TARJETA DEL INVENTARIO (SOLO LECTURA)
// ========================================
@Composable
fun InventoryCard(
    delivery: Delivery
) {
    val db = FirebaseFirestore.getInstance()
    var producerName by remember { mutableStateOf<String>("") }

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

    val dateString = remember(delivery.deliveryDate) {
        val ts = delivery.deliveryDate as? Timestamp
        if (ts != null)
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(ts.toDate())
        else
            delivery.analysisDate ?: "Sin fecha"
    }

    val moneyFormat = String.format(
        Locale("es", "CO"),
        "$%,.0f",
        delivery.totalPayment ?: 0.0
    )

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        dateString,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "COMPLETADO",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }

            // CUERPO
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Productor", color = Color.Gray)
                        Text(producerName, fontWeight = FontWeight.Bold)
                        Text(
                            "Lote: ${delivery.lotId}",
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.weight(1f))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Peso Bruto")
                            Text("${delivery.weightKgBruto ?: 0} kg", fontWeight = FontWeight.Bold)
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
                        label = { Text("Humedad") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = delivery.fermentationScore ?: "N/A",
                        onValueChange = {},
                        label = { Text("Fermentación") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.weight(1f)
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
                    Text(moneyFormat, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
