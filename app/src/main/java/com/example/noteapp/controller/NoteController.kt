package com.example.noteapp.controller

import com.example.noteapp.model.Note
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteController(private val database: DatabaseReference) {
    suspend fun saveNote(userId: String, note: Note, noteId: String? = null) {
        val noteData = mapOf(
            "tieude" to note.title,
            "noidung" to note.content,
            "imagePath" to note.imagePath
        )

        if (noteId == null) {
            database.child("notes").child(userId).push().setValue(noteData).await()
        } else {
            database.child("notes").child(userId).child(noteId).setValue(noteData).await()
        }
    }

    suspend fun deleteNote(userId: String, noteId: String) {
        database.child("notes").child(userId).child(noteId).removeValue().await()
    }

    suspend fun getNote(userId: String, noteId: String): Note? {
        return try {
            val snapshot = database.child("notes").child(userId).child(noteId).get().await()
            val data = snapshot.value as? Map<*, *> ?: return null

            Note(
                id = noteId,
                title = data["tieude"] as? String ?: "",
                content = data["noidung"] as? String ?: "",
                imagePath = data["imagePath"] as? String ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
    fun getNotesFlow(userId: String): Flow<List<Pair<String, Note>>> = callbackFlow {
        val notesRef = database.child("notes").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull {
                    val key = it.key ?: return@mapNotNull null
                    val value = it.value as? Map<*, *> ?: return@mapNotNull null
                    key to Note(
                        id = key,
                        title = value["tieude"] as? String ?: "",
                        content = value["noidung"] as? String ?: "",
                        imagePath = value["imagePath"] as? String ?: ""
                    )
                }
                trySend(notes).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        notesRef.addValueEventListener(listener)

        awaitClose {
            notesRef.removeEventListener(listener)
        }
    }
}