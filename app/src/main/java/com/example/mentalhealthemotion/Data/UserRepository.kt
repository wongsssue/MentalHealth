package com.example.mentalhealthemotion.Data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class UserRepository(
    private val userDao: UserDao,
    private val context: Context
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun initializeAdminUser() = withContext(Dispatchers.IO) {
        // Check if admin exists in Room
        val existingAdmin = userDao.findUserByRole(UserRole.ADMIN)

        if (existingAdmin == null) {
            val adminUser = User(
                userID = 1,
                userName = "wong",
                email = "wong@gmail.com",
                password = "1234",
                profilePicture = "",
                createdDate = "",
                accountStatus = AccountStatus.ACTIVE,
                phoneNo = "000-012-8596",
                birthDate = "",
                role = UserRole.ADMIN
            )

            userDao.insert(adminUser)

            if (isOnline()) {
                try {
                    // Write to Firestore
                    usersCollection.document(adminUser.userID.toString()).set(adminUser.toMap()).await()
                    Log.d("UserRepository", "Admin user initialized successfully in Firestore.")
                } catch (e: Exception) {
                    Log.e("UserRepository", "Failed to initialize admin user in Firestore: ${e.message}")
                }
            } else {
                Log.e("UserRepository", "No internet connection. Firestore write skipped.")
            }
        } else {
            Log.d("UserRepository", "Admin user already exists in Room. Verifying in Firestore...")

            if (isOnline()) {
                try {
                    // Check if admin exists in Firestore
                    val document = usersCollection.document(existingAdmin.userID.toString()).get().await()
                    if (!document.exists()) {
                        usersCollection.document(existingAdmin.userID.toString()).set(existingAdmin.toMap()).await()
                        Log.d("UserRepository", "Admin user synchronized successfully to Firestore.")
                    } else {
                        Log.d("UserRepository", "Admin user already exists in Firestore.")
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error verifying admin user in Firestore: ${e.message}")
                }
            } else {
                Log.e("UserRepository", "No internet connection. Firestore verification skipped.")
            }
        }
    }

    suspend fun registerUser(user: User) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                // Write to Firestore
                usersCollection.document(user.userID.toString()).set(user.toMap()).await()
                Log.d("UserRepository", "User registered successfully in Firestore.")
            }
            userDao.insert(user)  // Cache in Room
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to register user: ${e.message}")
        }
    }

    suspend fun loginUser(username: String, hashedPassword: String): User? = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val querySnapshot = usersCollection
                    .whereEqualTo("userName", username)
                    .get()
                    .await()

                val userMap = querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
                if (userMap != null && userMap.password == hashedPassword) {
                    return@withContext userMap
                }
            } else {
                // Check offline database
                val user = userDao.getUserByUsername(username).value
                if (user != null && user.password == hashedPassword) {
                    return@withContext user
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Login failed: ${e.message}")
        }
        null
    }

    suspend fun getAllUsers(): LiveData<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                if (isOnline()) {
                    try {
                        val querySnapshot = usersCollection.get().await()
                        val firestoreUsers = querySnapshot.documents.mapNotNull { document ->
                            document.toObject(User::class.java)
                        }

                        Log.d("UserRepository", "Users retrieved successfully from Firestore.")
                        // Convert Firestore users to LiveData
                        MutableLiveData<List<User>>().apply {
                            postValue(firestoreUsers)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "UserRepository",
                            "Failed to retrieve users from Firestore: ${e.message}"
                        )
                        // If Firestore fetch fails, fall back to Room
                        fetchUsersFromRoom()
                    }
                } else {
                    // Fallback to Room if offline
                    fetchUsersFromRoom()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "An error occurred: ${e.message}")
                throw e
            }
        }
    }

    // Helper function to fetch users from Room
    private fun fetchUsersFromRoom(): LiveData<List<User>> {
        return try {
            val usersFromRoom = userDao.getAllUsers()
            Log.d("UserRepository", "Users retrieved successfully from Room.")
            usersFromRoom
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to retrieve users from Room: ${e.message}")
            MutableLiveData<List<User>>() // Return an empty LiveData to avoid crashing
        }
    }

     fun saveImageToInternalStorage(user: User, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "${user.userID}-profile_picture.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream) // Copy the input stream (image) to the file
            outputStream.close()

            file.absolutePath // Return the file path
        } catch (e: Exception) {
            Log.e("ProfilePicture", "Failed to save image: ${e.message}")
            null
        }
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                usersCollection.document(user.userID.toString()).set(user.toMap()).await()
                Log.d("UserRepository", "User updated successfully in Firestore.")
            }
            userDao.updateUser(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update user: ${e.message}")
        }
    }

    suspend fun deleteUser(userId: Int) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                usersCollection.document(userId.toString()).delete().await()
                Log.d("UserRepository", "User deleted successfully in Firestore.")
            }
            userDao.deleteUser(userId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to delete user: ${e.message}")
        }
    }

    private fun User.toMap(): Map<String, Any?> {
        return mapOf(
            "userID" to userID,
            "userName" to userName,
            "email" to email,
            "password" to password,
            "profilePicture" to profilePicture,
            "createdDate" to createdDate,
            "accountStatus" to accountStatus.name,
            "phoneNo" to phoneNo,
            "birthDate" to birthDate,
            "role" to role.name
        )
    }


    suspend fun isEmailExist(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check in local Room database
                val isEmailInRoom = userDao.isEmailExists(email) > 0
                if (isEmailInRoom) return@withContext true

                // If online, check in Firestore
                if (isOnline()) {
                    val querySnapshot = usersCollection.whereEqualTo("email", email).get().await()
                    return@withContext querySnapshot.documents.isNotEmpty()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error checking email existence: ${e.message}")
            }
            false
        }
    }


    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val querySnapshot = usersCollection.whereEqualTo("email", email).get().await()
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val user = fromMap(document.data!!) // Convert Firestore data to User object
                    return@withContext user
                }
            }

            return@withContext userDao.getUserByEmail(email)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user by email: ${e.message}")
            return@withContext null
        }
    }


    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val querySnapshot = usersCollection.whereEqualTo("userID", userId).get().await()
                if (querySnapshot.documents.isNotEmpty()) {
                    // Get the first document matching the userID
                    val document = querySnapshot.documents[0]
                    // Convert the Firestore document to a User object
                    val user = fromMap(document.data!!)
                    return@withContext user
                }
            }
            // If not online or no user found in Firestore, fetch from the local database (Room)
            return@withContext userDao.getUserById(userId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user by ID: ${e.message}")
            return@withContext null
        }
    }

    private fun fromMap(data: Map<String, Any?>): User {
        return User(
            userID = (data["userID"] as? Long)?.toInt() ?: 0,
            userName = data["userName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            password = data["password"] as? String ?: "",
            profilePicture = data["profilePicture"] as? String?: "",
            createdDate = data["createdDate"] as? String ?: "",
            accountStatus = AccountStatus.valueOf(data["accountStatus"] as? String ?: "ACTIVE"),
            phoneNo = data["phoneNo"] as? String ?: "",
            birthDate = data["birthDate"] as? String ?: "",
            role = UserRole.valueOf(data["role"] as? String ?: "USER")
        )
    }

}
