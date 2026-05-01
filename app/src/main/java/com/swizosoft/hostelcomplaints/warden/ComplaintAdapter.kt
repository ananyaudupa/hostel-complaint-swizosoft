package com.swizosoft.hostelcomplaints.warden

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swizosoft.hostelcomplaints.R
import com.swizosoft.hostelcomplaints.databinding.ItemComplaintBinding
import com.swizosoft.hostelcomplaints.models.Complaint

class ComplaintAdapter(
    private val complaints: List<Complaint>,
    private val onClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemComplaintBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComplaintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val complaint = complaints[position]
        with(holder.binding) {
            categoryText.text = complaint.category
            descriptionText.text = complaint.description
            locationText.text = "Block: ${complaint.block}, Room: ${complaint.roomNumber}"
            statusText.text = "Status: ${complaint.status}"
            urgencyChip.text = complaint.urgency

            // Set urgency color
            val colorRes = when (complaint.urgency) {
                "Critical" -> R.color.errorContainer
                "High" -> android.R.color.holo_orange_light
                "Medium" -> android.R.color.holo_blue_light
                else -> android.R.color.darker_gray
            }
            urgencyChip.setChipBackgroundColorResource(colorRes)

            root.setOnClickListener { onClick(complaint) }
        }
    }

    override fun getItemCount() = complaints.size
}
