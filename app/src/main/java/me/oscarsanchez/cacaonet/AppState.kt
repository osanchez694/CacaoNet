package me.oscarsanchez.cacaonet

import androidx.compose.runtime.mutableStateOf

// Este objeto guarda el estado para toda la aplicación
object AppState {
    // Por defecto empezamos Offline (falso)
    var isOnline = mutableStateOf(false)
}