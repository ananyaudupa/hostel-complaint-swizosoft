package com.swizosoft.hostelcomplaints.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.swizosoft.hostelcomplaints.models.Complaint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ComplaintRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun submitComplaint(complaint: Complaint, mediaUris: List<Uri>): String = coroutineScope {
        // Upload media files in parallel
        val uploadTasks = mediaUris.map { uri ->
            async {
                val fileName = "complaints/${UUID.randomUUID()}"
                val ref = storage.reference.child(fileName)
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }
        }
        
        val uploadedUrls = uploadTasks.awaitAll()

        val finalComplaint = complaint.copy(
            id = firestore.collection("complaints").document().id,
            mediaUrls = uploadedUrls
        )

        firestore.collection("complaints").document(finalComplaint.id).set(finalComplaint).await()
        finalComplaint.id
    }

    suspend fun getStudentComplaints(studentId: String): List<Complaint> {
        val snapshot = firestore.collection("complaints")
            .whereEqualTo("studentId", studentId)
            .get().await()
        return snapshot.toObjects(Complaint::class.java)
    }
}
