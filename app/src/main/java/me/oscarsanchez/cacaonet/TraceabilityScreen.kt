package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class LoteCacao(
    val lote: String,
    val productor: String,
    val fecha: String,
    val clasificacion: String,
    val estado: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceabilityScreen(navController: NavController) {
    val lotesDemo = listOf(
        LoteCacao("L-001", "Productor A", "01/11/2025", "Fino", "En bodega"),
        LoteCacao("L-002", "Productor B", "02/11/2025", "Exportación", "En tránsito"),
        LoteCacao("L-003", "Productor C", "03/11/2025", "Corriente", "Entregado")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trazabilidad de lotes") },
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lotesDemo) { lote ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Lote #: ${lote.lote}")
                        Text("Productor: ${lote.productor}")
                        Text("Fecha: ${lote.fecha}")
                        Text("Clasificación: ${lote.clasificacion}")
                        Text("Estado del cacao: ${lote.estado}")
                    }
                }
            }
        }
    }
}
