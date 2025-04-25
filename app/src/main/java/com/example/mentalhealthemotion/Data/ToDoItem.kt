package com.example.mentalhealthemotion.Data


import com.google.firebase.firestore.PropertyName

data class ToDoItem(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("task") @set:PropertyName("task") var task: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", false, System.currentTimeMillis())
}
