package com.example.noteapp.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.noteapp.controller.NoteController
import kotlinx.coroutines.launch

@Composable
fun DisplayScreen(
    noteController: NoteController,
    userId: String,
    onEdit: (String) -> Unit,
    onNavigateToInput: () -> Unit,
    onLogout: () -> Unit
) {
    val notes by noteController.getNotesFlow(userId).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Màu sắc
    val backgroundColor = Color(0xFFFFEB3B) // Màu vàng
    val noteCardColor = Color(0xFFD2B48C)   // Màu vàng nâu (tan)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Danh sách ghi chú:",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes) { (noteId, note) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onEdit(noteId) },
                        colors = CardDefaults.cardColors(
                            containerColor = noteCardColor
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Tiêu đề: ${note.title}",
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Nội dung: ${note.content.take(20)}...",
                                color = Color.Black
                            )

                            if (note.imagePath.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(note.imagePath),
                                        contentDescription = "Ảnh đính kèm",
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onEdit(noteId) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF5D4037)
                                    )
                                ) {
                                    Text("Chỉnh sửa")
                                }

                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            noteController.deleteNote(userId, noteId)
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF5D4037)
                                    )
                                ) {
                                    Text("Xóa")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5D4037),
                        contentColor = Color.White
                    )
                ) {
                    Text("Đăng xuất")
                }

                Button(
                    onClick = onNavigateToInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5D4037),
                        contentColor = Color.White
                    )
                ) {
                    Text("Thêm ghi chú")
                }
            }
        }
    }
}