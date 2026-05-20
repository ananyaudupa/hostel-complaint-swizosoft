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

    enum class StepState {
        COMPLETED,
        CURRENT,
        PENDING
    }

    private fun displayStatus(complaint: Complaint) {
        binding.categoryText.text = complaint.category
        
        if (complaint.wardenRemarks.isNotEmpty()) {
            binding.remarksLabel.visibility = View.VISIBLE
            binding.remarksText.visibility = View.VISIBLE
            binding.remarksText.text = complaint.wardenRemarks
        }

        updateStep(binding.stepSubmitted.root, "Submitted", "We have received your complaint.", getStepState(complaint.status, "Submitted"))
        updateStep(binding.stepAssigned.root, "Assigned", "Assigned to maintenance staff.", getStepState(complaint.status, "Assigned"))
        updateStep(binding.stepInProgress.root, "In Progress", "Work is currently underway.", getStepState(complaint.status, "In Progress"))
        updateStep(binding.stepResolved.root, "Resolved", "Issue has been fixed.", getStepState(complaint.status, "Resolved"))
    }

    private fun updateStep(view: View, title: String, subtitle: String, state: StepState) {
        view.findViewById<TextView>(R.id.stepTitle).text = title
        view.findViewById<TextView>(R.id.stepSubtitle).text = subtitle
        val indicator = view.findViewById<ImageView>(R.id.statusIndicator)
        
        when (state) {
            StepState.COMPLETED -> {
                indicator.setImageResource(R.drawable.ic_timeline_checked)
                indicator.clearColorFilter()
            }
            StepState.CURRENT -> {
                indicator.setImageResource(android.R.drawable.presence_online)
                indicator.setColorFilter(getColor(R.color.primary))
            }
            StepState.PENDING -> {
                indicator.setImageResource(android.R.drawable.presence_invisible)
                indicator.setColorFilter(getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun getStepState(currentStatus: String, targetStep: String): StepState {
        val order = listOf("Submitted", "Assigned", "In Progress", "Resolved")
        val currentIndex = order.indexOf(currentStatus)
        val targetIndex = order.indexOf(targetStep)
        
        return when {
            targetIndex < currentIndex || (currentStatus == "Resolved" && targetIndex == currentIndex) -> StepState.COMPLETED
            targetIndex == currentIndex -> StepState.CURRENT
            else -> StepState.PENDING
        }
    }
}
