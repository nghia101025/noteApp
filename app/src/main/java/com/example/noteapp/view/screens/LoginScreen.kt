package com.example.noteapp.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.noteapp.R
import com.example.noteapp.controller.AuthController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authController: AuthController,
    onLoginSuccess: (String) -> Unit
) {
    var inputId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.note),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
            )
        }

        // Khu vực form đăng nhập
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SnackbarHost(hostState = snackbarHostState)

            OutlinedTextField(
                value = inputId,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        inputId = newValue
                    }
                },
                label = { Text("Nhập ID (4 chữ số)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (inputId.length != 4) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("ID phải chính xác 4 chữ số")
                        }
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            if (authController.loginOrRegister(inputId)) {
                                onLoginSuccess(inputId)
                            } else {
                                snackbarHostState.showSnackbar("Đăng nhập thất bại")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Lỗi: ${e.message}")
                        }
                    }
                },
                enabled = inputId.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.Yellow
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("XÁC NHẬN")
            }
        }
    }
}