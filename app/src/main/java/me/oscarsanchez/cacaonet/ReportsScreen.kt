package me.oscarsanchez.cacaonet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// =======================
//  MODELO DE DATOS
// =======================
data class Delivery(
    val id: String = "",
    val producerId: String = "",
    val lotId: String = "",
    val weightKgBruto: Double? = null,
    val deliveryDate: Any? = null,
    val status: String? = null,
    val operatorId: String? = null,
    val analysisDate: String? = null,
    val moisturePercentage: Double? = null,
    val fermentationScore: String? = null,
    val qualityGrade: String? = null,
    val pricePerKg: Double? = null,
    val totalPayment: Double? = null,
    val paymentStatus: String? = null,
)

// =======================
//  PANTALLA PRINCIPAL (ReportsScreen)
// =======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    // Datos principales
    var allDeliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var filteredDeliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }

    // Mapa para guardar los nombres: { "ID_USUARIO" : "Juan Perez" }
    var producersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Estados de UI
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ---------------------------------------------------------
    // 1. FUNCIÓN PARA CARGAR PRODUCTORES (PARA PODER BUSCARLOS)
    // ---------------------------------------------------------
    fun loadProducersMap(onComplete: () -> Unit) {
        db.collection("producers").get()
            .addOnSuccessListener { snapshot ->
                val tempMap = mutableMapOf<String, String>()
                snapshot.documents.forEach { doc ->
                    // Busca el nombre en varios campos posibles
                    val name = doc.getString("producerName")
                        ?: doc.getString("nombre")
                        ?: doc.getString("name")
                        ?: "Desconocido"
                    tempMap[doc.id] = name
                }

                // También cargamos users por si acaso
                db.collection("users").get().addOnSuccessListener { userSnap ->
                    userSnap.documents.forEach { doc ->
                        if (!tempMap.containsKey(doc.id)) {
                            val name = doc.getString("nombre") ?: "Usuario"
                            tempMap[doc.id] = name
                        }
                    }
                    producersMap = tempMap
                    onComplete()
                }
            }
            .addOnFailureListener { onComplete() }
    }

    // ---------------------------------------------------------
    // 2. FUNCIÓN PARA CARGAR ENTREGAS
    // ---------------------------------------------------------
    fun loadDeliveries() {
        isLoading = true
        errorMessage = null

        // Primero cargamos el mapa de nombres, luego las entregas
        loadProducersMap {
            db.collection("deliveries")
                .orderBy("deliveryDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val list = snapshot.documents.map { doc ->
                            fun num(field: String): Double? {
                                val d = doc.getDouble(field)
                                if (d != null) return d
                                val l = doc.getLong(field)
                                return l?.toDouble()
                            }

                            Delivery(
                                id = doc.id,
                                producerId = doc.getString("producerId") ?: "",
                                lotId = doc.getString("lotId") ?: "",
                                weightKgBruto = num("weightKg_Bruto"),
                                deliveryDate = doc.get("deliveryDate"),
                                status = doc.getString("status"),
                                moisturePercentage = num("moisturePercentage"),
                                fermentationScore = doc.getString("fermentationScore") ?: doc.get("fermentationScore")?.toString(),
                                qualityGrade = doc.getString("qualityGrade"),
                                pricePerKg = num("pricePerKg"),
                                totalPayment = num("totalPayment"),
                                paymentStatus = doc.getString("paymentStatus"),
                            )
                        }
                        allDeliveries = list
                        filteredDeliveries = list // Inicialmente mostramos todo
                        isLoading = false
                    } catch (e: Exception) {
                        errorMessage = "Error leyendo datos: ${e.message}"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = "Error de conexión: ${e.message}"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        loadDeliveries()
    }

    // ---------------------------------------------------------
    // 3. LÓGICA DE FILTRADO (BUSCADOR POTENTE)
    // ---------------------------------------------------------
    LaunchedEffect(searchText, allDeliveries, producersMap) {
        if (searchText.isBlank()) {
            filteredDeliveries = allDeliveries
        } else {
            val query = searchText.lowercase()
            filteredDeliveries = allDeliveries.filter { delivery ->

                // A) Buscar por LOTE
                val matchLote = delivery.lotId.lowercase().contains(query)

                // B) Buscar por NOMBRE DEL PRODUCTOR (Usando el mapa)
                val producerName = producersMap[delivery.producerId]?.lowercase() ?: ""
                val matchName = producerName.contains(query)

                // C) Buscar por FECHA (Formateada)
                val date = (delivery.deliveryDate as? Timestamp)?.toDate()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
                val dateStr = if (date != null) dateFormat.format(date) else ""
                val matchDate = dateStr.contains(query) // Ej. "20/11"

                // Si coincide cualquiera de los 3, se muestra
                matchLote || matchName || matchDate
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text("Reportes y Análisis", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { loadDeliveries() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                        }
                    }
                )

                // BARRA DE BÚSQUEDA MEJORADA
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar productor, fecha o lote...") }, // <--- INDICACIÓN CLARA
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        { IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) } }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                errorMessage != null -> {
                    Text(errorMessage!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }

                filteredDeliveries.isEmpty() -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron resultados", color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredDeliveries) { delivery ->
                            // Obtenemos el nombre del mapa para pasarlo a la tarjeta
                            val name = producersMap[delivery.producerId]
                                ?: if (delivery.producerId.length < 10) delivery.producerId else "Sin Nombre"

                            DeliveryEditableCard(
                                delivery = delivery,
                                producerNameResolved = name, // Pasamos el nombre ya resuelto
                                onSave = { updated ->
                                    // --- LÓGICA DE GUARDADO ---
                                    val pricePerKg = when (updated.qualityGrade) {
                                        "Exportación" -> 12500.0
                                        "Nacional Estándar" -> 9500.0
                                        "Rechazo" -> 1000.0
                                        else -> updated.pricePerKg ?: 0.0
                                    }

                                    val netWeight = updated.weightKgBruto?.let { bruto ->
                                        val hum = updated.moisturePercentage ?: 0.0
                                        bruto * (1 - hum / 100.0)
                                    }

                                    val totalPayment = if (netWeight != null) netWeight * pricePerKg else updated.totalPayment

                                    db.collection("deliveries")
                                        .document(updated.id)
                                        .update(
                                            mapOf(
                                                "status" to "Análisis Completo",
                                                "moisturePercentage" to updated.moisturePercentage,
                                                "fermentationScore" to updated.fermentationScore,
                                                "qualityGrade" to updated.qualityGrade,
                                                "pricePerKg" to pricePerKg,
                                                "totalPayment" to totalPayment,
                                                "analysisDate" to Timestamp.now()
                                            )
                                        )
                                        .addOnSuccessListener { loadDeliveries() }
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
//  TARJETA DE DISEÑO MEJORADO
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryEditableCard(
    delivery: Delivery,
    producerNameResolved: String, // Recibimos el nombre desde afuera
    onSave: (Delivery) -> Unit
) {
    // Formatear fecha
    val dateString = remember(delivery.deliveryDate) {
        try {
            val date = (delivery.deliveryDate as? Timestamp)?.toDate()
            if (date != null) {
                SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(date)
            } else {
                "Sin fecha"
            }
        } catch (e: Exception) { "Error fecha" }
    }

    // Formatear moneda
    val moneyFormat = remember(delivery.totalPayment) {
        try { String.format(Locale("es", "CO"), "$%,.0f", delivery.totalPayment ?: 0.0) } catch (e: Exception) { "$0" }
    }

    // Estados de edición local
    var moistureText by remember(delivery.id) { mutableStateOf(delivery.moisturePercentage?.toString() ?: "") }
    var fermentation by remember(delivery.id) { mutableStateOf(delivery.fermentationScore ?: "") }
    var quality by remember(delivery.id) { mutableStateOf(delivery.qualityGrade ?: "") }

    val qualityOptions = listOf("Exportación", "Nacional Estándar", "Rechazo")
    var expanded by remember { mutableStateOf(false) }

    // Colores
    val isCompleted = delivery.status == "Análisis Completo"
    val headerColor = if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0) // Verde suave o Naranja suave
    val statusText = if (isCompleted) "COMPLETADO" else "PENDIENTE DE ANÁLISIS"
    val statusTextColor = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, null, modifier = Modifier.size(16.dp), tint = statusTextColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateString, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = statusTextColor)
                }
                Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusTextColor)
            }

            // --- CUERPO ---
            Column(modifier = Modifier.padding(16.dp)) {

                // Información Principal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Productor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        // USAMOS EL NOMBRE QUE VIENE DEL FILTRO
                        Text(producerNameResolved, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Lote: ${delivery.lotId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Peso Bruto", style = MaterialTheme.typography.labelSmall)
                            Text("${delivery.weightKgBruto ?: 0} kg", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Inputs de Análisis
                Text("Datos de Calidad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("Humedad %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fermentation,
                        onValueChange = { fermentation = it },
                        label = { Text("Ferment.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = quality.ifEmpty { "Seleccione Calidad" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clasificación Final") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        qualityOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { quality = selectionOption; expanded = false }
                            )
                        }
                    }
                }
            }

            // --- FOOTER DE PAGO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Precio Base:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("$${String.format("%,.0f", delivery.pricePerKg ?: 0.0)} /kg", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL A PAGAR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(moneyFormat, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val updated = delivery.copy(
                            moisturePercentage = moistureText.toDoubleOrNull(),
                            fermentationScore = fermentation,
                            qualityGrade = quality
                        )
                        onSave(updated)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar Análisis y Precio")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}