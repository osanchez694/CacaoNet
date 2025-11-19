package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDeliveryScreen(navController: NavController) {

    // Instancia de Firestore
    val db = FirebaseFirestore.getInstance()

    // Estados de los campos
    var productor by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var humedad by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipoCacao by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar entrega de cacao") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = productor,
                onValueChange = { productor = it },
                label = { Text("Nombre del productor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = humedad,
                onValueChange = { humedad = it },
                label = { Text("Humedad (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha") },
                placeholder = { Text("dd/mm/aaaa") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tipoCacao,
                onValueChange = { tipoCacao = it },
                label = { Text("Tipo de cacao") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Mapa con los datos que se van a guardar
                    val entrega = hashMapOf(
                        "productor" to productor,
                        "pesoKg" to peso.toDoubleOrNull(),
                        "humedad" to humedad.toDoubleOrNull(),
                        "fecha" to fecha,
                        "tipoCacao" to tipoCacao
                    )

                    db.collection("entregas")
                        .add(entrega)
                        .addOnSuccessListener {
                            // Si todo sale bien, volvemos atrás
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            // Aquí podrías mostrar un Snackbar o Log.e si quieres
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }
        }
    }
}
