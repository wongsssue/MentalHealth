package com.example.mentalhealthemotion.Data

data class ProfileField(
    val icon: Int,
    val text: String,
    val description: String,
    val onEdit: () -> Unit
)
