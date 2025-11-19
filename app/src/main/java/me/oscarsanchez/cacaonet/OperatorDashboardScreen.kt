package me.oscarsanchez.cacaonet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Modelo de datos simple para configurar los botones del menú
data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color = Color(0xFF6750A4)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    navController: NavController
) {
    // Definimos los items del menú
    val menuItems = listOf(
        DashboardItem("Reportes", Icons.Default.DateRange, Screen.Reports.route),
        DashboardItem("Inventario", Icons.Default.List, Screen.Inventory.route),
        DashboardItem("Pagos", Icons.Default.ShoppingCart, Screen.Payments.route),
        DashboardItem("Productores", Icons.Default.Person, Screen.Producers.route)
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel de Operador",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val pudoVolver = navController.popBackStack()
                            if (!pudoVolver) {
                                navController.navigate(Screen.Login.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // ---------------------------------------------------------
                    // AQUÍ ESTÁ LA CORRECCIÓN: LÓGICA DINÁMICA DE ESTADO
                    // ---------------------------------------------------------

                    // 1. Leemos el estado global (asegúrate de haber creado AppState.kt)
                    val isOnline = AppState.isOnline.value

                    // 2. Configuramos colores y textos según el estado
                    val chipColor = if (isOnline) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                    val dotColor = if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    val textColor = if (isOnline) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                    val textStatus = if (isOnline) "Online" else "Offline"

                    // 3. Dibujamos el botón
                    Surface(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { navController.navigate(Screen.Offline.route) },
                        shape = RoundedCornerShape(16.dp),
                        color = chipColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // El puntito de color (Verde o Rojo)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // El texto (Online u Offline)
                            Text(
                                text = textStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hola, bienvenido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "¿Qué deseas hacer hoy?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Grid de Opciones
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(menuItems) { item ->
                    DashboardCard(item) {
                        navController.navigate(item.route)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(160.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}