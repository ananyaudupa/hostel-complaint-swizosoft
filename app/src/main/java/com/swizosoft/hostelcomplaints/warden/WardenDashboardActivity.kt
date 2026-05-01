package com.swizosoft.hostelcomplaints.warden

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.swizosoft.hostelcomplaints.auth.LoginActivity
import com.swizosoft.hostelcomplaints.databinding.ActivityWardenDashboardBinding
import com.swizosoft.hostelcomplaints.models.Complaint

class WardenDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWardenDashboardBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var allComplaints = mutableListOf<Complaint>()
    private var complaintListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardenDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        startListeningForComplaints()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(com.swizosoft.hostelcomplaints.R.menu.warden_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                com.swizosoft.hostelcomplaints.R.id.action_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.complaintsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterComplaints(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun startListeningForComplaints() {
        binding.progressBar.visibility = View.VISIBLE
        
        complaintListener = firestore.collection("complaints")
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(this, "Firebase Error: ${error.message}", Toast.LENGTH_LONG).show()
                    Log.e("WardenDashboard", "Error fetching complaints", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Log the raw size to see if any documents are returned at all
                    val rawSize = snapshot.size()
                    Log.d("WardenDashboard", "Raw documents found: $rawSize")
                    
                    allComplaints = snapshot.toObjects(Complaint::class.java).toMutableList()
                    Log.d("WardenDashboard", "Mapped complaints: ${allComplaints.size}")

                    allComplaints.sortWith(compareBy { 
                        when(it.urgency) {
                            "Critical" -> 0
                            "High" -> 1
                            "Medium" -> 2
                            "Low" -> 3
                            else -> 4
                        }
                    })

                    filterComplaints(binding.tabLayout.selectedTabPosition)
                }
            }
    }

    private fun filterComplaints(tabIndex: Int) {
        val filtered = if (tabIndex == 0) {
            // Show all that are NOT Resolved or Rejected
            allComplaints.filter { it.status != "Resolved" && it.status != "Rejected" }
        } else {
            // Show Resolved or Rejected
            allComplaints.filter { it.status == "Resolved" || it.status == "Rejected" }
        }
        
        Log.d("WardenDashboard", "Filtered items for tab $tabIndex: ${filtered.size}")

        binding.complaintsRecyclerView.adapter = ComplaintAdapter(filtered) { complaint ->
            val intent = Intent(this, ComplaintDetailActivity::class.java)
            intent.putExtra("complaint_id", complaint.id)
            startActivity(intent)
        }
        
        if (filtered.isEmpty()) {
            binding.complaintsRecyclerView.visibility = View.GONE
            Toast.makeText(this, "No complaints in this category ($tabIndex)", Toast.LENGTH_SHORT).show()
        } else {
            binding.complaintsRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        complaintListener?.remove()
    }
}
