package me.oscarsanchez.cacaonet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
//  PANTALLA DE INVENTARIO (NUEVA)
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var deliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- FUNCIÓN DE CARGA (Mantiene el filtro de "Análisis Completo") ---
    fun loadInventoryDeliveries() {
        isLoading = true
        errorMessage = null

        db.collection("deliveries")
            // FILTRO CLAVE: Solo lotes con análisis completado
            .whereEqualTo("status", "Análisis Completo")
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val list = snapshot.documents.map { doc ->
                        // Funciones auxiliares para parsear datos (se mantienen)
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
                            // ✅ USAMOS LA NUEVA TARJETA CON EL DISEÑO DE REPORTES
                            InventoryCard(delivery = delivery)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
//  ✅ NUEVO COMPONENTE: TARJETA DE INVENTARIO (Diseño de Reportes, Solo Lectura)
// =========================================================================
@Composable
fun InventoryCard(
    delivery: Delivery
) {
    val db = FirebaseFirestore.getInstance()
    var producerName by remember { mutableStateOf<String?>(null) }

    // Lógica para obtener el nombre del productor (Se mantiene)
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

    // Formatear fecha (Se mantiene)
    val dateString = remember(delivery.deliveryDate) {
        // Asumiendo que delivery.deliveryDate ya se formateó en loadInventoryDeliveries()
        // o si es de tipo String/Any ya casteado, usamos el campo analysisDate si existe.
        delivery.analysisDate ?: delivery.deliveryDate?.toString()?.substringBefore(" ") ?: "Sin fecha"
    }


    // Formatear moneda (Se mantiene)
    val moneyFormat = remember(delivery.totalPayment) {
        try { String.format(Locale("es", "CO"), "$%,.0f", delivery.totalPayment ?: 0.0) } catch (e: Exception) { "$0" }
    }

    // Colores y textos
    val headerColor = Color(0xFFE8F5E9) // Verde suave
    val statusText = "COMPLETADO"
    val statusTextColor = Color(0xFF2E7D32) // Verde oscuro

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // --- HEADER (Igual que Reportes) ---
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

                // Información Principal (Igual que Reportes)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Productor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(producerName ?: delivery.producerId, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Lote: ${delivery.lotId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Peso Bruto", style = MaterialTheme.typography.labelSmall)
                            Text("${delivery.weightKgBruto ?: 0.0} kg", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // ✅ Mostramos los campos de ANÁLISIS, pero en MODO SOLO LECTURA
                Text("Datos de Calidad (Análisis)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Humedad
                    OutlinedTextField(
                        value = "${delivery.moisturePercentage ?: "N/A"} %",
                        onValueChange = {},
                        label = { Text("Humedad") },
                        readOnly = true, // Es la clave del solo lectura
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(disabledContainerColor = Color.Transparent, disabledTextColor = Color.Black),
                        enabled = false // Deshabilita la interacción
                    )
                    // Fermentación
                    OutlinedTextField(
                        value = delivery.fermentationScore ?: "N/A",
                        onValueChange = {},
                        label = { Text("Fermentación") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(disabledContainerColor = Color.Transparent, disabledTextColor = Color.Black),
                        enabled = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clasificación Final (con OutlinedTextField para mantener el diseño)
                OutlinedTextField(
                    value = delivery.qualityGrade ?: "N/A",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clasificación Final") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(disabledContainerColor = Color.Transparent, disabledTextColor = Color.Black),
                    enabled = false
                )
            }

            // --- FOOTER DE PAGO (Igual que Reportes, sin el botón de Guardar) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Precio por kg:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("$${String.format("%,.0f", delivery.pricePerKg ?: 0.0)} /kg", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL PAGADO", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(moneyFormat, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                }

                // ✅ Mostramos el estado de pago, que es clave para Inventario
                Text("Estado de Pago: ${delivery.paymentStatus ?: "N/A"}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}