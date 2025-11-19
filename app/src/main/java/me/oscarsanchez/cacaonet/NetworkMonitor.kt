package me.oscarsanchez.cacaonet

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun NetworkMonitorInit() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Función robusta para chequear estado actual
        fun checkConnectivity() {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            // Verificamos si tiene capacidad de INTERNET y VALIDATED
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            // Actualizamos el estado global
            AppState.isOnline.value = isConnected
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // A veces se conecta al wifi pero aun no tiene internet real, esperamos un poco o validamos
                checkConnectivity()
            }

            override fun onLost(network: Network) {
                AppState.isOnline.value = false
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                checkConnectivity()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Chequeo inicial al cargar la app
        checkConnectivity()

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}