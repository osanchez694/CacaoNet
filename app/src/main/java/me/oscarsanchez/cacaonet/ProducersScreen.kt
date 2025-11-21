package me.oscarsanchez.cacaonet

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

// ----------------------------------------------------------------------
// 1. Modelo de datos (Mantener igual, pero se puede mover a un archivo si es necesario)
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

    // 2. Estado para la búsqueda
    var searchText by remember { mutableStateOf("") }
    var producers by remember { mutableStateOf<List<Producer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔄 Lógica de carga de datos (Se mantiene igual)
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

    // 3. Lógica de Filtrado
    val filteredProducers = remember(producers, searchText) {
        if (searchText.isBlank()) {
            producers
        } else {
            val lowerCaseQuery = searchText.lowercase()
            producers.filter { producer ->
                producer.producerName.lowercase().contains(lowerCaseQuery) ||
                        producer.phone.contains(lowerCaseQuery) // Búsqueda por teléfono
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productores") },
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
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 4. Barra de Búsqueda
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

            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre barra y lista

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
                    // 5. Lista de Productores Filtrada
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        items(filteredProducers) { producer ->
                            ProducerCard(producer = producer)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// 6. Componente de Tarjeta de Productor con Diseño Mejorado
// ----------------------------------------------------------------------
@Composable
fun ProducerCard(producer: Producer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Nombre y Tipo (Fila superior)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nombre del Productor (Más destacado)
                Text(
                    text = producer.producerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Badge de Tipo
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
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

            // Teléfono y Hectáreas (Fila inferior)
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