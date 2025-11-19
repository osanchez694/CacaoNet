package me.oscarsanchez.cacaonet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDeliveryScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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

            OutlinedTextField(
                value = productor,
                onValueChange = { productor = it },
                label = { Text("Nombre del productor") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = humedad,
                onValueChange = { humedad = it },
                label = { Text("Humedad (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tipoCacao,
                onValueChange = { tipoCacao = it },
                label = { Text("Tipo de cacao") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

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
                            // volver atrás cuando se guarde
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            // Mostrar error si deseas
                        }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }
        }
    }
}
