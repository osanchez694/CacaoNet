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

// --- PALETA DE COLORES INTENSA (CACAO) ---
val DeepChocolate = Color(0xFF3E2723) // Café muy oscuro (Barra superior)
val MilkChocolate = Color(0xFF5D4037) // Café medio (Textos)
val LatteBackground = Color(0xFFD7CCC8) // Fondo de la pantalla (Café con leche)
val CreamCard = Color(0xFFFFF8E1)       // Fondo de las tarjetas (Crema suave)

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    navController: NavController
) {
    val menuItems = listOf(
        DashboardItem("Reportes", Icons.Default.DateRange, Screen.Reports.route),
        DashboardItem("Inventario", Icons.Default.List, Screen.Inventory.route),
        DashboardItem("Pagos", Icons.Default.ShoppingCart, Screen.Payments.route),
        DashboardItem("Productores", Icons.Default.Person, Screen.Producers.route)
    )

    Scaffold(
        containerColor = LatteBackground, // <--- Fondo color Café con Leche
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CacaoNet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // Texto blanco sobre fondo oscuro
                        )
                        Text(
                            text = "Panel de Operador",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBCAAA4) // Un beige grisáceo para el subtítulo
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    // Lógica de estado Online/Offline
                    val isOnline = AppState.isOnline.value

                    // Ajustamos los colores del Chip para que se vean bien sobre el café oscuro
                    val chipColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    val dotColor = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val textStatus = if (isOnline) "Online" else "Offline"
                    val textColor = if (isOnline) Color(0xFF1B5E20) else Color(0xFFB71C1C)

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
                            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = textStatus, style = MaterialTheme.typography.labelMedium, color = textColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepChocolate // <--- Barra color Chocolate Oscuro
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
            Spacer(modifier = Modifier.height(24.dp))

            // Saludo con más contraste
            Text(
                text = "Bienvenido, Operador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DeepChocolate // Texto oscuro fuerte
            )
            Text(
                text = "Selecciona una opción para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MilkChocolate, // Texto café medio
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamCard // <--- Tarjeta color Crema
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculo de fondo para el icono
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DeepChocolate.copy(alpha = 0.1f)), // Círculo café transparente
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = DeepChocolate, // Icono Chocolate oscuro
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepChocolate, // Texto Chocolate oscuro
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}