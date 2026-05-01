package com.swizosoft.hostelcomplaints.complaints

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.swizosoft.hostelcomplaints.ai.GeminiService
import com.swizosoft.hostelcomplaints.data.AuthRepository
import com.swizosoft.hostelcomplaints.data.ComplaintRepository
import com.swizosoft.hostelcomplaints.databinding.ActivitySubmitComplaintBinding
import com.swizosoft.hostelcomplaints.models.Complaint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubmitComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitComplaintBinding
    private val auth = FirebaseAuth.getInstance()
    private val complaintRepo = ComplaintRepository()
    private val authRepo = AuthRepository()
    private lateinit var geminiService: GeminiService
    
    private val selectedMediaUris = mutableListOf<Uri>()
    private var aiAnalysisJob: Job? = null
    private var predictedCategory = ""
    private var predictedUrgency = "Medium"
    private var predictedSentiment = "Neutral"

    private val mediaPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedMediaUris.add(uri)
                Toast.makeText(this, "Media added. Total: ${selectedMediaUris.size}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geminiService = GeminiService(this)

        setupCategoryDropdown()
        setupListeners()
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Electricity", "Water", "WiFi", "Plumbing", "Furniture", "Cleanliness", "Security", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        binding.categoryDropdown.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                if (text.length > 10) {
                    debounceAiAnalysis(text)
                }
            }
        })

        binding.addMediaBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "audio/*"))
            }
            mediaPickerLauncher.launch(intent)
        }

        binding.askAiBtn.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        binding.submitBtn.setOnClickListener {
            submit()
        }
    }

    private fun debounceAiAnalysis(text: String) {
        aiAnalysisJob?.cancel()
        aiAnalysisJob = lifecycleScope.launch {
            delay(1000)
            binding.aiAnalysisCard.visibility = View.VISIBLE
            binding.aiResultText.text = "AI is analyzing your complaint..."
            
            val result = geminiService.analyzeComplaint(text)
            predictedCategory = result.category
            predictedUrgency = result.urgency
            predictedSentiment = result.sentiment
            
            binding.aiResultText.text = "Suggested Category: ${result.category}\nPredicted Urgency: ${result.urgency}\nSentiment: ${result.sentiment}"
            
            if (binding.categoryDropdown.text.isEmpty()) {
                binding.categoryDropdown.setText(result.category, false)
            }
        }
    }

    private fun submit() {
        val description = binding.descriptionInput.text.toString().trim()
        val category = binding.categoryDropdown.text.toString()

        if (description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Submitting")
            .setMessage("Uploading evidence and saving your complaint... Please wait.")
            .setCancelable(false)
            .create()
        
        dialog.show()
        binding.submitBtn.isEnabled = false

        lifecycleScope.launch {
            val user = authRepo.getUserDetails(auth.currentUser?.uid ?: "")
            if (user == null) {
                dialog.dismiss()
                Toast.makeText(this@SubmitComplaintActivity, "User session error", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val complaint = Complaint(
                studentId = user.uid,
                studentName = user.name,
                category = category,
                description = description,
                block = user.block,
                roomNumber = user.roomNumber,
                urgency = predictedUrgency,
                sentiment = predictedSentiment
            )

            try {
                complaintRepo.submitComplaint(complaint, selectedMediaUris)
                dialog.dismiss()
                showSuccessDialog()
            } catch (e: Exception) {
                dialog.dismiss()
                binding.submitBtn.isEnabled = true
                Toast.makeText(this@SubmitComplaintActivity, "Submission failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success!")
            .setMessage("Your complaint has been submitted successfully. The Warden will be notified.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}
