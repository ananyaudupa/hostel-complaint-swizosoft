package com.swizosoft.hostelcomplaints.models

data class Complaint(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val category: String = "", // Electricity, Water, etc.
    val description: String = "",
    val block: String = "",
    val roomNumber: String = "",
    val urgency: String = "Medium", // Low, Medium, High, Critical
    val status: String = "Submitted", // Submitted, Assigned, In Progress, Resolved, Rejected
    val mediaUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val sentiment: String = "Neutral", // AI predicted sentiment
    val wardenRemarks: String = "",
    val assignedStaff: String = ""
)
