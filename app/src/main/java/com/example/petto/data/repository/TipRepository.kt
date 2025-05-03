package com.example.petto.data.repository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.petto.data.model.Tip
import kotlinx.coroutines.tasks.await

object TipRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getTips(): List<Tip> {
        return try {
            val snapshot = db.collection("Tips").get().await()
            snapshot.documents.map { doc ->
                Tip(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    url = doc.getString("url") // ✅ include this line

                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}