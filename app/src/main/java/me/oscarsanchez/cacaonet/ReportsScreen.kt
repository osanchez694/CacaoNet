package me.oscarsanchez.cacaonet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Entrega(
    val productor: String,
    val pesoKg: Double,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {

    val entregasDemo = listOf(
        Entrega("Productor A", 100.0, "01/11/2025"),
        Entrega("Productor B", 80.0, "02/11/2025"),
        Entrega("Productor C", 120.0, "03/11/2025")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
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

            Text("Gráfico simple (simulado)")
            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {

                val barWidth = size.width / 7
                val maxHeight = size.height

                drawLine(
                    color = Color.DarkGray,
                    start = Offset(barWidth, maxHeight),
                    end = Offset(barWidth, maxHeight - 60f),
                    strokeWidth = barWidth
                )

                drawLine(
                    color = Color.DarkGray,
                    start = Offset(3 * barWidth, maxHeight),
                    end = Offset(3 * barWidth, maxHeight - 90f),
                    strokeWidth = barWidth
                )

                drawLine(
                    color = Color.DarkGray,
                    start = Offset(5 * barWidth, maxHeight),
                    end = Offset(5 * barWidth, maxHeight - 40f),
                    strokeWidth = barWidth
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tabla de entregas")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(entregasDemo) { entrega ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entrega.productor)
                        Text("${entrega.pesoKg} kg")
                        Text(entrega.fecha)
                    }
                    Divider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) { Text("PDF") }
                Button(onClick = { }) { Text("Excel") }
            }
        }
    }
}
