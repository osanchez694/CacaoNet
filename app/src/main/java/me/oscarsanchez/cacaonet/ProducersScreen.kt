package me.oscarsanchez.cacaonet

import androidx.compose.foundation.clickable // <-- IMPORTANTE: Importar el modificador clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
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
// 1. Definición de Colores Personalizados
// ----------------------------------------------------------------------
val SoftCream = Color(0xFFFFF8E1) // Color crema suave para el fondo
val DarkBrown = Color(0xFF411B1B) // Color marrón oscuro para la barra superior
val PressedCardColor = Color(0xFFE0E0E0) // Nuevo color para el estado presionado

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

    // NUEVO ESTADO: Para controlar el diálogo y qué productor se selecciona
    var selectedProducer by remember { mutableStateOf<Producer?>(null) }

    // 🔄 Lógica de carga de datos
    LaunchedEffect(Unit) {
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
                // ESTILO DEL ENCABEZADO
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
            // Barra de Búsqueda
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
                            // MODIFICADO: Añadido el lambda para manejar el clic
                            ProducerCard(producer = producer, onClick = {
                                selectedProducer = producer // Guarda el productor para mostrar el diálogo
                            })
                        }
                    }
                }
            }
        }
    }

    // NUEVO: Mostrar el diálogo si hay un productor seleccionado
    selectedProducer?.let { producer ->
        ProducerDetailDialog(
            producer = producer,
            onDismiss = { selectedProducer = null } // Cierra el diálogo y resetea el estado
        )
    }
}

// ----------------------------------------------------------------------
// 3. Componente de Diálogo con el Detalle del Productor (NUEVO)
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
        containerColor = SoftCream // Usando tu color crema suave para el fondo del diálogo
    )
}

// ----------------------------------------------------------------------
// 4. Componente de Tarjeta de Productor (MODIFICADO)
// ----------------------------------------------------------------------
@Composable
fun ProducerCard(producer: Producer, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Lógica para cambiar el color cuando se presiona (para un efecto visual rápido)
    val cardColor = if (isPressed) {
        PressedCardColor // Color gris más oscuro para el estado de "presionado"
    } else {
        Color(0xFFEFEFEF) // Gris muy claro por defecto
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // MODIFICADO: Añadido el comportamiento de clic y la fuente de interacción
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Deshabilita la ondulación por defecto (ripple) para usar el cambio de color de la tarjeta
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor // Usa el color dinámico
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

                // Badge de Tipo
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = producer.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
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