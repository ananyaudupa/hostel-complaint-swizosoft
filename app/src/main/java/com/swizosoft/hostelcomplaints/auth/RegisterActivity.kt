package com.swizosoft.hostelcomplaints.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.swizosoft.hostelcomplaints.MainActivity
import com.swizosoft.hostelcomplaints.data.AuthRepository
import com.swizosoft.hostelcomplaints.databinding.ActivityRegisterBinding
import com.swizosoft.hostelcomplaints.models.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleToggles()

        binding.registerBtn.setOnClickListener {
            register()
        }
    }

    private fun setupRoleToggles() {
        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.wardenRadio.id) {
                binding.blockLayout.visibility = View.GONE
                binding.roomLayout.visibility = View.GONE
            } else {
                binding.blockLayout.visibility = View.VISIBLE
                binding.roomLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun register() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val role = if (binding.studentRadio.isChecked) "STUDENT" else "WARDEN"
        
        var block = binding.blockInput.text.toString().trim()
        var room = binding.roomInput.text.toString().trim()

        // Basic validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Conditional validation for Students
        if (role == "STUDENT" && (block.isEmpty() || room.isEmpty())) {
            Toast.makeText(this, "Please enter your Block and Room Number", Toast.LENGTH_SHORT).show()
            return
        }

        // Defaults for Warden
        if (role == "WARDEN") {
            block = "N/A"
            room = "Admin"
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.registerBtn.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    val user = User(uid, name, email, role, block, room)
                    
                    lifecycleScope.launch {
                        try {
                            repository.saveUserDetails(user)
                            binding.progressBar.visibility = View.GONE
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            binding.progressBar.visibility = View.GONE
                            binding.registerBtn.isEnabled = true
                            Toast.makeText(this@RegisterActivity, "Error saving user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.registerBtn.isEnabled = true
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
