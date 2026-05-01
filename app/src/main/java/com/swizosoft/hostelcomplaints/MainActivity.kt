package com.swizosoft.hostelcomplaints

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.swizosoft.hostelcomplaints.auth.LoginActivity
import com.swizosoft.hostelcomplaints.data.AuthRepository
import com.swizosoft.hostelcomplaints.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        
        lifecycleScope.launch {
            val user = repository.getUserDetails(uid)
            if (user != null) {
                binding.textView.text = "Welcome, ${user.name}"
                setupDashboard(user.role)
            } else {
                Toast.makeText(this@MainActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                auth.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupDashboard(role: String) {
        if (role == "WARDEN") {
            binding.wardenDashboardBtn.visibility = View.VISIBLE
            binding.wardenDashboardBtn.setOnClickListener {
                startActivity(Intent(this, com.swizosoft.hostelcomplaints.warden.WardenDashboardActivity::class.java))
            }
        } else {
            binding.submitComplaintBtn.visibility = View.VISIBLE
            binding.viewComplaintsBtn.visibility = View.VISIBLE
            
            binding.submitComplaintBtn.setOnClickListener {
                startActivity(Intent(this, com.swizosoft.hostelcomplaints.complaints.SubmitComplaintActivity::class.java))
            }
            
            binding.viewComplaintsBtn.setOnClickListener {
                startActivity(Intent(this, com.swizosoft.hostelcomplaints.complaints.ComplaintHistoryActivity::class.java))
            }
        }
    }
}
