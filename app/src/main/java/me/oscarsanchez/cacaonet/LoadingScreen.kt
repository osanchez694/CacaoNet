package me.oscarsanchez.cacaonet

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoadingScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(true) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val tipo = doc.getString("tipo")?.uppercase()

                when (tipo) {
                    "OPERADOR" -> navController.navigate(Screen.OperatorDashboard.route)
                    "PRODUCTOR" -> navController.navigate(Screen.ProducerDashboard.route)
                    "COMPRADOR" -> navController.navigate(Screen.BuyerDashboard.route)
                    else -> navController.navigate(Screen.Login.route)
                }
            }
            .addOnFailureListener {
                navController.navigate(Screen.Login.route)
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
