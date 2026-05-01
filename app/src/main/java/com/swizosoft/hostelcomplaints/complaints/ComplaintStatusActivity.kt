package com.swizosoft.hostelcomplaints.complaints

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.swizosoft.hostelcomplaints.R
import com.swizosoft.hostelcomplaints.databinding.ActivityComplaintStatusBinding
import com.swizosoft.hostelcomplaints.models.Complaint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ComplaintStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintStatusBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var complaintId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        complaintId = intent.getStringExtra("complaint_id")
        loadComplaintStatus()
    }

    private fun loadComplaintStatus() {
        val id = complaintId ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("complaints").document(id).get().await()
                val complaint = snapshot.toObject(Complaint::class.java)
                if (complaint != null) {
                    displayStatus(complaint)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ComplaintStatusActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayStatus(complaint: Complaint) {
        binding.categoryText.text = complaint.category
        
        if (complaint.wardenRemarks.isNotEmpty()) {
            binding.remarksLabel.visibility = View.VISIBLE
            binding.remarksText.visibility = View.VISIBLE
            binding.remarksText.text = complaint.wardenRemarks
        }

        updateStep(binding.stepSubmitted.root, "Submitted", "We have received your complaint.", true)
        updateStep(binding.stepAssigned.root, "Assigned", "Assigned to maintenance staff.", isReached(complaint.status, "Assigned"))
        updateStep(binding.stepInProgress.root, "In Progress", "Work is currently underway.", isReached(complaint.status, "In Progress"))
        updateStep(binding.stepResolved.root, "Resolved", "Issue has been fixed.", isReached(complaint.status, "Resolved"))
    }

    private fun updateStep(view: View, title: String, subtitle: String, active: Boolean) {
        view.findViewById<TextView>(R.id.stepTitle).text = title
        view.findViewById<TextView>(R.id.stepSubtitle).text = subtitle
        val indicator = view.findViewById<ImageView>(R.id.statusIndicator)
        
        if (active) {
            indicator.setImageResource(android.R.drawable.presence_online)
            indicator.setColorFilter(getColor(R.color.primary))
        } else {
            indicator.setImageResource(android.R.drawable.presence_invisible)
            indicator.setColorFilter(getColor(android.R.color.darker_gray))
        }
    }

    private fun isReached(currentStatus: String, targetStep: String): Boolean {
        val order = listOf("Submitted", "Assigned", "In Progress", "Resolved")
        val currentIndex = order.indexOf(currentStatus)
        val targetIndex = order.indexOf(targetStep)
        return currentIndex >= targetIndex
    }
}
