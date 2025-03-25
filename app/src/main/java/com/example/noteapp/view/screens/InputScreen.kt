package com.example.noteapp.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.noteapp.controller.NoteController
import com.example.noteapp.model.Note
import com.example.noteapp.utils.saveImageToInternalStorage
import kotlinx.coroutines.launch

@Composable
fun InputScreen(
    noteController: NoteController,
    userId: String,
    noteId: String?,
    onNavigateToDisplay: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    imagePath = saveImageToInternalStorage(context, it)
                } catch (e: Exception) {
                    errorMessage = "Không thể tải ảnh: ${e.message}"
                }
            }
        }
    )

    LaunchedEffect(noteId) {
        if (noteId != null) {
            isLoading = true
            try {
                val note = noteController.getNote(userId, noteId)
                note?.let {
                    title = it.title
                    content = it.content
                    imagePath = it.imagePath.ifEmpty { null }
                }
            } catch (e: Exception) {
                errorMessage = "Không thể tải ghi chú: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SnackbarHost(hostState = snackbarHostState)

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                modifier = Modifier.fillMaxWidth(),
                isError = title.isEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Nội dung") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                isError = content.isEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Chọn ảnh")
            }

            Spacer(modifier = Modifier.height(8.dp))

            imagePath?.let { path ->
                Column {
                    Image(
                        painter = rememberAsyncImagePainter(path),
                        contentDescription = "Ảnh đã chọn",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { imagePath = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Xóa ảnh")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigateToDisplay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hủy")
                }

                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            errorMessage = "Vui lòng nhập tiêu đề và nội dung"
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                val note = Note(
                                    id = noteId ?: "",
                                    title = title,
                                    content = content,
                                    imagePath = imagePath ?: ""
                                )

                                noteController.saveNote(userId, note, noteId)
                                snackbarHostState.showSnackbar(
                                    if (noteId != null) "Cập nhật thành công" else "Tạo mới thành công"
                                )
                                onNavigateToDisplay()
                            } catch (e: Exception) {
                                errorMessage = "Lỗi khi lưu: ${e.message}"
                            }
                        }
                    },
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text(if (noteId != null) "Cập nhật" else "Lưu")
                }
            }
        }
    }
}