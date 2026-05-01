package com.swizosoft.hostelcomplaints.warden

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.swizosoft.hostelcomplaints.databinding.ActivityComplaintDetailBinding
import com.swizosoft.hostelcomplaints.models.Complaint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ComplaintDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintDetailBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var complaintId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        complaintId = intent.getStringExtra("complaint_id")
        
        setupStatusDropdown()
        loadComplaintDetails()

        binding.updateBtn.setOnClickListener {
            updateStatus()
        }
    }

    private fun setupStatusDropdown() {
        val statuses = arrayOf("Submitted", "Assigned", "In Progress", "Resolved", "Rejected")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statuses)
        binding.statusDropdown.setAdapter(adapter)
    }

    private fun loadComplaintDetails() {
        val id = complaintId ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("complaints").document(id).get().await()
                val complaint = snapshot.toObject(Complaint::class.java)
                if (complaint != null) {
                    displayDetails(complaint)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ComplaintDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayDetails(complaint: Complaint) {
        with(binding) {
            categoryText.text = complaint.category
            urgencyChip.text = complaint.urgency
            studentInfo.text = "Student: ${complaint.studentName} (${complaint.block}, ${complaint.roomNumber})"
            descriptionText.text = complaint.description
            statusDropdown.setText(complaint.status, false)
            remarksInput.setText(complaint.wardenRemarks)

            // Setup Media RecyclerView (Simple implementation)
            if (complaint.mediaUrls.isNotEmpty()) {
                mediaRecyclerView.adapter = MediaAdapter(complaint.mediaUrls)
            } else {
                mediaLabel.visibility = View.GONE
                mediaRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun updateStatus() {
        val id = complaintId ?: return
        val newStatus = binding.statusDropdown.text.toString()
        val remarks = binding.remarksInput.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                firestore.collection("complaints").document(id).update(
                    mapOf(
                        "status" to newStatus,
                        "wardenRemarks" to remarks
                    )
                ).await()
                Toast.makeText(this@ComplaintDetailActivity, "Status updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ComplaintDetailActivity, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
