package me.oscarsanchez.cacaonet

import androidx.compose.runtime.mutableStateOf

object AppState {
    // Es CRUCIAL usar 'mutableStateOf' para que Compose detecte el cambio
    var isOnline = mutableStateOf(true)
}