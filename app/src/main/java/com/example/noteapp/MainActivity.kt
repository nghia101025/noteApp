package com.example.noteapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noteapp.ui.theme.NoteAppTheme
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import coil.compose.rememberAsyncImagePainter


class MainActivity : ComponentActivity() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteAppTheme {
                AppNavigator(database)
            }
        }
    }
}

@Composable
fun AppNavigator(database: DatabaseReference) {
    var currentScreen by remember { mutableStateOf("login") }
    var userId by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "login" -> LoginScreen(database) { id ->
            userId = id
            currentScreen = "display"
        }
        "display" -> DisplayScreen(database, userId, onEdit = { noteId ->
            selectedNoteId = noteId
            currentScreen = "input"
        }, onNavigateToInput = {
            selectedNoteId = null
            currentScreen = "input"
        }, onLogout = {
            userId = ""
            currentScreen = "login"
        })
        "input" -> {
            InputScreen(database, userId, selectedNoteId) {
                currentScreen = "display"
            }
        }
    }
}
@Composable
fun LoginScreen(database: DatabaseReference, onLoginSuccess: (String) -> Unit) {
    var inputId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        SnackbarHost(hostState = snackbarHostState)

        TextField(value = inputId, onValueChange = { inputId = it }, label = { Text("Nhập ID của bạn") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (inputId.isNotBlank()) {
                database.child("users").child(inputId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        onLoginSuccess(inputId)
                    } else {
                        database.child("users").child(inputId).setValue(true).addOnSuccessListener {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Tạo ID mới thành công!") }
                            onLoginSuccess(inputId)
                        }
                    }
                }
            }
        }) {
            Text("Xác nhận")
        }
    }
}

@Composable
fun InputScreen(database: DatabaseReference, userId: String, noteId: String?, onNavigateToDisplay: () -> Unit) {
    var tieude by remember { mutableStateOf("") }
    var noidung by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(context, it)
            imagePath = savedPath
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (!noteId.isNullOrEmpty()) {
            database.child("notes").child(userId).child(noteId).get()
                .addOnSuccessListener { snapshot ->
                    val data = snapshot.value as? Map<String, Any>
                    tieude = data?.get("tieude") as? String ?: ""
                    noidung = data?.get("noidung") as? String ?: ""
                    imagePath = data?.get("imagePath") as? String
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        SnackbarHost(hostState = snackbarHostState)

        TextField(value = tieude, onValueChange = { tieude = it }, label = { Text("Tiêu đề") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = noidung, onValueChange = { noidung = it }, label = { Text("Nội dung") }, modifier = Modifier.fillMaxWidth().height(200.dp))
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Chọn ảnh")
        }

        imagePath?.let {
            Image(painter = rememberAsyncImagePainter(it), contentDescription = "Ảnh đã chọn", modifier = Modifier.size(100.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateToDisplay) {
                Text("Thoát")
            }

            Button(onClick = {
                if (tieude.isNotBlank() && noidung.isNotBlank()) {
                    val note = mapOf("tieude" to tieude, "noidung" to noidung, "imagePath" to (imagePath ?: ""))
                    if (noteId == null) {
                        database.child("notes").child(userId).push().setValue(note)
                    } else {
                        database.child("notes").child(userId).child(noteId).setValue(note)
                    }
                    coroutineScope.launch { snackbarHostState.showSnackbar("Lưu thành công!") }
                    onNavigateToDisplay()
                }
            }) {
                Text("Lưu")
            }
        }
    }
}



// Hàm lưu ảnh vào bộ nhớ trong
fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    val file = File(context.filesDir, "images")
    if (!file.exists()) file.mkdir()

    val filePath = File(file, "${System.currentTimeMillis()}.jpg")

    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(filePath)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return filePath.absolutePath
}



@Composable
fun DisplayScreen(
    database: DatabaseReference,
    userId: String,
    onEdit: (String) -> Unit,
    onNavigateToInput: () -> Unit,
    onLogout: () -> Unit
) {
    var notes by remember { mutableStateOf(emptyList<Pair<String, Map<String, String>>>()) }

    LaunchedEffect(userId) {
        database.child("notes").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = snapshot.children.mapNotNull {
                    val key = it.key ?: return@mapNotNull null
                    val value = it.value as? Map<String, String> ?: return@mapNotNull null
                    key to value
                }
                notes = dataList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Danh sách ghi chú:", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f) // Cho phép cuộn và tránh lấn chiếm không gian
            ) {
                items(notes) { (noteId, note) ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onEdit(noteId) }) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "Tiêu đề: ${note["tieude"] ?: "Không có"}")
                            Text(text = "Nội dung: ${note["noidung"]?.take(20) ?: "Không có"}...")

                            note["imagePath"]?.let { path ->
                                if (path.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(path),
                                        contentDescription = "Ảnh đính kèm",
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEdit(noteId) }) { Text("Chỉnh sửa") }
                                TextButton(onClick = {
                                    database.child("notes").child(userId).child(noteId).removeValue()
                                }) { Text("Xóa") }


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
                Button(onClick = onLogout) {
                    Text("Đăng xuất")
                }

                Button(onClick = onNavigateToInput) {
                    Text("Thêm ghi chú")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    NoteAppTheme {
        AppNavigator(FirebaseDatabase.getInstance().reference)
    }
}
