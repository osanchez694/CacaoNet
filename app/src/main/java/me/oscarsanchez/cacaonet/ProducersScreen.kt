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
import androidx.compose.material.icons.filled.Refresh // <-- NUEVO: Importar el icono de Refresh
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
// 1. Definición de Colores Personalizados (ACTUALIZADO: Colores para el badge)
// ----------------------------------------------------------------------
val SoftCream = Color(0xFFFFF8E1) // Color crema suave para el fondo
val DarkBrown = Color(0xFF411B1B) // Color marrón oscuro para la barra superior
val PressedCardColor = Color(0xFFE0E0E0) // Nuevo color para el estado presionado

// Colores para el Badge (AJUSTADOS para que coincidan con la imagen que enviaste)
val BadgeBackgroundColor = Color(0xFFE5F1E1) // Verde muy claro, similar al de tu imagen
val BadgeContentColor = Color(0xFF5A8E54)    // Verde oscuro para el texto del badge


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

    // Función para cargar los productores (NUEVA: para reutilizarla)
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

    // 🔄 Lógica de carga de datos inicial
    LaunchedEffect(Unit) {
        loadProducers()
    }

    // Lógica de Filtrado (por Nombre o Teléfono)
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
        containerColor = SoftCream,
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
                // NUEVO: Añadido el botón de refresh
                actions = {
                    IconButton(onClick = { loadProducers() }) { // Llama a la función de carga al presionar
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar lista",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown
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
            Text(producer.producerName, fontWeight = FontWeight.Bold, color = DarkBrown)
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
        containerColor = SoftCream
    )
}

// ----------------------------------------------------------------------
// 4. Componente de Tarjeta de Productor (Badge con colores ajustados)
// ----------------------------------------------------------------------
@Composable
fun ProducerCard(producer: Producer, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardColor = if (isPressed) {
        PressedCardColor
    } else {
        Color(0xFFEFEFEF) // Gris muy claro por defecto
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Badge de Tipo (ACTUALIZADO: Usando los nuevos colores definidos)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BadgeBackgroundColor // <-- Color de fondo ajustado
                ) {
                    Text(
                        text = producer.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = BadgeContentColor, // <-- Color de texto ajustado
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
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
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