package com.example.noteapp.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

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