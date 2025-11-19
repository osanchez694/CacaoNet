package me.oscarsanchez.cacaonet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (UserType) -> Unit
) {
    // Colores definidos dentro de la función
    val DeepChocolate = Color(0xFF3E2723)
    val DeepChocolateFaded = Color(0x803E2723)
    val LatteBackground = Color(0xFFD3C8C4)
    val CreamCard = Color(0xFFD3C8C4)
    val CreamCardFaded = Color(0xFFD3C8C4)
    val MediumCoffee = Color(0xFF5D4037)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf(UserType.OPERADOR) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LatteBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- LOGO AGRANDADO ---
            Image(
                painter = painterResource(id = R.drawable.ic_cacaonet_logo),
                contentDescription = "Logo de CacaoNet",
                modifier = Modifier
                    .size(150.dp) // <--- Tamaño aumentado
                    .padding(bottom = 8.dp) // Espaciado menor al texto para que estén juntos
            )
            // ---------------------

            Text(
                text = "CacaoNet",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepChocolate
            )

            Text(
                text = "Bienvenido de nuevo",
                style = MaterialTheme.typography.bodyLarge,
                color = MediumCoffee,
                modifier = Modifier.padding(bottom = 48.dp) // Espaciado antes de los campos
            )

            // Campo Usuario
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Usuario (correo)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepChocolate,
                    unfocusedBorderColor = DeepChocolateFaded,
                    focusedLabelColor = DeepChocolate,
                    cursorColor = DeepChocolate,
                    focusedContainerColor = CreamCard,
                    unfocusedContainerColor = CreamCardFaded
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepChocolate,
                    unfocusedBorderColor = DeepChocolateFaded,
                    focusedLabelColor = DeepChocolate,
                    cursorColor = DeepChocolate,
                    focusedContainerColor = CreamCard,
                    unfocusedContainerColor = CreamCardFaded
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selecciona tu perfil:",
                style = MaterialTheme.typography.labelLarge,
                color = DeepChocolate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                // Chip Productor
                FilterChip(
                    selected = selectedUserType == UserType.PRODUCTOR,
                    onClick = { selectedUserType = UserType.PRODUCTOR },
                    label = { Text("Productor") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepChocolate,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        labelColor = DeepChocolate
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DeepChocolate,
                        selectedBorderColor = DeepChocolate,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = selectedUserType == UserType.PRODUCTOR
                    )
                )

                // Chip Operador
                FilterChip(
                    selected = selectedUserType == UserType.OPERADOR,
                    onClick = { selectedUserType = UserType.OPERADOR },
                    label = { Text("Operador") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepChocolate,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        labelColor = DeepChocolate
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DeepChocolate,
                        selectedBorderColor = DeepChocolate,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = selectedUserType == UserType.OPERADOR
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val mail = email.trim()
                    val pass = password.trim()

                    if (mail.isEmpty() || pass.isEmpty()) {
                        errorMessage = "Por favor ingresa correo y contraseña."
                        return@Button
                    }

                    isLoading = true
                    auth.signInWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLogin(selectedUserType)
                            } else {
                                errorMessage = "Error: Verifica tus credenciales."
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepChocolate,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}