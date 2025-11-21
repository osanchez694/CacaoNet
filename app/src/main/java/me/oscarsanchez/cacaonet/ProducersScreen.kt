package me.oscarsanchez.cacaonet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

// ----------------------------------------------------------------------
// 1. Definición de Colores Personalizados (Solo se dejan los del Badge, el resto se incrustan)
// ----------------------------------------------------------------------

// Colores para el Badge
val BadgeBackgroundColor = Color(0xFFE5F1E1)
val BadgeContentColor = Color(0xFF5A8E54)


// ----------------------------------------------------------------------
// 2. Modelo de datos
// ----------------------------------------------------------------------
data class Producer(
    val id: String = "",
    val producerName: String = "",
    val fincaName: String = "",
    val phone: String = "",
    val hectares: Long? = null,
    val type: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducersScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var searchText by remember { mutableStateOf("") }
    var producers by remember { mutableStateOf<List<Producer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedProducer by remember { mutableStateOf<Producer?>(null) }

    val loadProducers: () -> Unit = {
        loading = true
        error = null
        db.collection("producers")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.map { doc ->
                    Producer(
                        id = doc.id,
                        producerName = doc.getString("producerName") ?: "",
                        fincaName = doc.getString("fincaName") ?: "",
                        phone = doc.getString("phone") ?: "",
                        hectares = doc.getLong("hectares"),
                        type = doc.getString("type") ?: ""
                    )
                }
                producers = list
                loading = false
            }
            .addOnFailureListener { e ->
                error = e.message
                loading = false
            }
    }

    LaunchedEffect(Unit) {
        loadProducers()
    }

    val filteredProducers = remember(producers, searchText) {
        if (searchText.isBlank()) {
            producers
        } else {
            val lowerCaseQuery = searchText.lowercase()
            producers.filter { producer ->
                producer.producerName.lowercase().contains(lowerCaseQuery) ||
                        producer.phone.contains(lowerCaseQuery)
            }
        }
    }

    Scaffold(
        // Fondo de pantalla: LatteBackground (0xFFD7CCC8)
        containerColor = Color(0xFFD7CCC8),
        topBar = {
            TopAppBar(
                title = {
                    Text("Productores", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loadProducers() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar lista",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Color de la barra superior: DeepChocolate (0xFF3E2723)
                    containerColor = Color(0xFF3E2723)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar por nombre o teléfono") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(
                        text = "Error al cargar productores: ${error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                filteredProducers.isEmpty() -> {
                    Text(
                        "No se encontraron productores.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        items(filteredProducers) { producer ->
                            ProducerCard(producer = producer, onClick = {
                                selectedProducer = producer
                            })
                        }
                    }
                }
            }
        }
    }

    selectedProducer?.let { producer ->
        ProducerDetailDialog(
            producer = producer,
            onDismiss = { selectedProducer = null }
        )
    }
}

// ----------------------------------------------------------------------
// 3. Componente de Diálogo con el Detalle del Productor
// ----------------------------------------------------------------------
@Composable
fun ProducerDetailDialog(producer: Producer, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                producer.producerName,
                fontWeight = FontWeight.Bold,
                // Color del título: DeepChocolate (0xFF3E2723)
                color = Color(0xFF3E2723)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo: ${producer.type}")
                Divider()
                Text("Finca: ${producer.fincaName}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Teléfono", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(producer.phone, fontWeight = FontWeight.SemiBold)
                }
                producer.hectares?.let {
                    Text("🌾 Hectáreas: $it")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        // Fondo del diálogo: CreamCard (0xFFFFF8E1)
        containerColor = Color(0xFFFFF8E1)
    )
}

// ----------------------------------------------------------------------
// 4. Componente de Tarjeta de Productor
// ----------------------------------------------------------------------
@Composable
fun ProducerCard(producer: Producer, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Color de la tarjeta normal: CreamCard (0xFFFFF8E1)
    val CardColorNormal = Color(0xFFFFF8E1)
    // Color de la tarjeta presionada: CardColorPressed (0xFFF0E5D3)
    val CardColorPressed = Color(0xFFF0E5D3)

    val cardColor = if (isPressed) {
        CardColorPressed
    } else {
        CardColorNormal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Nombre y Tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = producer.producerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    // Color del título: DeepChocolate (0xFF3E2723)
                    color = Color(0xFF3E2723),
                    modifier = Modifier.weight(weight = 1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Badge de Tipo
                Surface(
                    shape = RoundedCornerShape(size = 4.dp),
                    color = BadgeBackgroundColor
                ) {
                    Text(
                        text = producer.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = BadgeContentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Finca
            Text(
                text = "Finca: ${producer.fincaName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Teléfono y Hectáreas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Teléfono
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Teléfono",
                        modifier = Modifier.size(size = 16.dp),
                        // Color del icono: DeepChocolate (0xFF3E2723)
                        tint = Color(0xFF3E2723)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = producer.phone,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Hectáreas
                producer.hectares?.let {
                    Text(
                        text = "🌾 ${it} Hectáreas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}