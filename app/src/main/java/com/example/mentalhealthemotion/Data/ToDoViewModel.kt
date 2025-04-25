package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import com.example.mentalhealthemotion.Data.ToDoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ToDoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _tasks = MutableStateFlow<List<ToDoItem>>(emptyList())
    val tasks: StateFlow<List<ToDoItem>> = _tasks

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        db.collection("tasks").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _tasks.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ToDoItem::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    fun addTask(task: String) {
        val newTask = ToDoItem(id = "", task = task, isCompleted = false, timestamp = System.currentTimeMillis())
        db.collection("tasks").add(newTask)
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        db.collection("tasks").document(taskId).update("isCompleted", isCompleted)
    }

    fun deleteTask(taskId: String) {
        db.collection("tasks").document(taskId).delete()
    }
}
