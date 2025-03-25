package com.example.noteapp.view

import androidx.compose.runtime.*
import com.example.noteapp.controller.AuthController
import com.example.noteapp.controller.NoteController
import com.example.noteapp.view.screens.DisplayScreen
import com.example.noteapp.view.screens.InputScreen
import com.example.noteapp.view.screens.LoginScreen
import com.google.firebase.database.DatabaseReference

@Composable
fun AppNavigator(database: DatabaseReference) {
    var currentScreen by remember { mutableStateOf("login") }
    var userId by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    val authController = remember { AuthController(database) }
    val noteController = remember { NoteController(database) }

    when (currentScreen) {
        "login" -> LoginScreen(
            authController = authController,
            onLoginSuccess = { id ->
                userId = id
                currentScreen = "display"
            }
        )
        "display" -> DisplayScreen(
            noteController = noteController,
            userId = userId,
            onEdit = { noteId ->
                selectedNoteId = noteId
                currentScreen = "input"
            },
            onNavigateToInput = {
                selectedNoteId = null
                currentScreen = "input"
            },
            onLogout = {
                userId = ""
                currentScreen = "login"
            }
        )
        "input" -> InputScreen(
            noteController = noteController,
            userId = userId,
            noteId = selectedNoteId,
            onNavigateToDisplay = {
                currentScreen = "display"
            }
        )
    }
}