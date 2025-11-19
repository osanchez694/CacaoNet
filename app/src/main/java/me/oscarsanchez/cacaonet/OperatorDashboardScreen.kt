package me.oscarsanchez.cacaonet

import androidx.compose.foundation.background
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

// --- PALETA DE COLORES ---
val DeepChocolate = Color(0xFF3E2723)
val MilkChocolate = Color(0xFF5D4037)
val LatteBackground = Color(0xFFD7CCC8)
val CreamCard = Color(0xFFFFF8E1)

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

    // LECTURA REACTIVA: Compose repintará esto automáticamente cuando AppState cambie
    val isOnline = AppState.isOnline.value

    Scaffold(
        containerColor = LatteBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CacaoNet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Panel de Operador",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBCAAA4)
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
                    // Lógica visual del estado
                    val chipColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    val dotColor = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val textStatus = if (isOnline) "Online" else "Offline"
                    val textColor = if (isOnline) Color(0xFF1B5E20) else Color(0xFFB71C1C)

                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = chipColor
                        // ELIMINADO: .clickable { navigate(Offline) }
                        // RAZÓN: Queremos que la app siga funcionando, no que nos lleve a una pantalla de error.
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
                    containerColor = DeepChocolate
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
            // Aviso extra si está offline (Opcional, pero útil para el usuario)
            if (!isOnline) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color(0xFFB71C1C),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Modo Offline: Los datos se guardarán en el dispositivo.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Bienvenido, Operador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DeepChocolate
            )
            Text(
                text = "Selecciona una opción para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MilkChocolate,
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
                    // IMPORTANTE: Aquí permitimos navegar SIEMPRE, haya o no internet.
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
            containerColor = CreamCard
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DeepChocolate.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = DeepChocolate,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepChocolate,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}