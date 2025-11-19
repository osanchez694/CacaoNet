package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

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

    var producers by remember { mutableStateOf<List<Producer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔄 Leer la colección "producers" de Firestore
    LaunchedEffect(Unit) {
        db.collection("producers")          // 👈 nombre EXACTO de la colección
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
                .padding(16.dp)
        ) {

            when {
                loading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(
                        text = "Error al cargar productores: ${error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                producers.isEmpty() -> {
                    Text("No hay productores registrados.")
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(producers) { producer ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = producer.producerName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Finca: ${producer.fincaName}")
                                    Text("Teléfono: ${producer.phone}")
                                    producer.hectares?.let {
                                        Text("Hectáreas: $it")
                                    }
                                    Text("Tipo: ${producer.type}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
