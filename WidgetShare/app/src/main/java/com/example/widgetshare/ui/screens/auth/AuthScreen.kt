package com.example.widgetshare.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.widgetshare.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isLogin) "Sign In" else "Sign Up",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (!isLogin) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        if (isLogin) {
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    navController.navigate("friends") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    errorMessage = it.localizedMessage
                                    isLoading = false
                                }
                        } else {
                            if (nickname.isBlank()) {
                                errorMessage = "Nickname is required"
                                isLoading = false
                                return@Button
                            }
                            scope.launch {
                                try {
                                    val exists = userRepository.findUserByNickname(nickname)
                                    if (exists != null) {
                                        errorMessage = "Nickname already taken"
                                        isLoading = false
                                        return@launch
                                    }
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { result ->
                                            scope.launch {
                                                try {
                                                    userRepository.createUserProfile(result.user!!.uid, email, nickname)
                                                    navController.navigate("friends") {
                                                        popUpTo("auth") { inclusive = true }
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = e.localizedMessage
                                                }
                                                isLoading = false
                                            }
                                        }
                                        .addOnFailureListener {
                                            errorMessage = it.localizedMessage
                                            isLoading = false
                                        }
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(if (isLogin) "Sign In" else "Sign Up")
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { isLogin = !isLogin; errorMessage = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In")
                }
            }
        }
    }
} 