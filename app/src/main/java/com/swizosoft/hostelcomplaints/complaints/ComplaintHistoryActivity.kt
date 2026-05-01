package com.swizosoft.hostelcomplaints.complaints

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.swizosoft.hostelcomplaints.data.ComplaintRepository
import com.swizosoft.hostelcomplaints.databinding.ActivityComplaintHistoryBinding
import com.swizosoft.hostelcomplaints.warden.ComplaintAdapter
import kotlinx.coroutines.launch

class ComplaintHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintHistoryBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = ComplaintRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadHistory() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val complaints = repository.getStudentComplaints(uid)
                if (complaints.isEmpty()) {
                    binding.emptyText.visibility = View.VISIBLE
                } else {
                    binding.emptyText.visibility = View.GONE
                    // Reuse the Warden's ComplaintAdapter for now
                    binding.historyRecyclerView.adapter = ComplaintAdapter(complaints) { complaint ->
                        val intent = Intent(this@ComplaintHistoryActivity, ComplaintStatusActivity::class.java).apply {
                            putExtra("complaint_id", complaint.id)
                        }
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ComplaintHistoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
