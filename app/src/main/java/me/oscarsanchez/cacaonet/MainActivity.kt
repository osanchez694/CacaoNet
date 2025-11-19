package me.oscarsanchez.cacaonet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import me.oscarsanchez.cacaonet.ui.theme.CacaoNetTheme // Asegúrate que este import sea correcto según tu proyecto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CacaoNetTheme {
                // --- AQUÍ ESTÁ LA CLAVE ---
                // Esto enciende el monitor de red para toda la aplicación
                NetworkMonitorInit()
                // --------------------------

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Aquí llamas a tu sistema de navegación o pantalla principal
                    // Por ejemplo:
                    // NavigationWrapper() o LoginScreen() o OperatorDashboardScreen(...)

                    // Si no tienes navegación centralizada aún y estás probando directo:
                    val navController = androidx.navigation.compose.rememberNavController()
                    OperatorDashboardScreen(navController = navController)
                }
            }
        }
    }
}