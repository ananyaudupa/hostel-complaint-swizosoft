package com.swizosoft.hostelcomplaints.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT", // "STUDENT" or "WARDEN"
    val block: String = "",
    val roomNumber: String = "",
    val phoneNumber: String = ""
)
