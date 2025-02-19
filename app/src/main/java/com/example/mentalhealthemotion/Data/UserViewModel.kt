package com.example.mentalhealthemotion.Data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Patterns
import android.widget.Toast
import com.google.android.gms.nearby.connection.AuthenticationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Backing properties (mutableStateOf)
    private var _username = mutableStateOf("")
    private var _email = mutableStateOf("")
    private var _password = mutableStateOf("")
    private var _confirmPassword = mutableStateOf("")
    private var _role = mutableStateOf(UserRole.NORMAL)
    private var _accountStatus = mutableStateOf(AccountStatus.ACTIVE)

    // Exposing immutable state to the UI
    var username: State<String> = _username
    var email: State<String> = _email
    var password: State<String> = _password
    var confirmPassword: State<String> = _confirmPassword
    var role: State<UserRole> = _role
    var accountStatus: State<AccountStatus> = _accountStatus

    // Update username value
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    // Update email value
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    // Update password value
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    // Update confirm password value
    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    // Update role
    fun updateRole(newRole: UserRole) {
        _role.value = newRole
    }

    // Update account status
    fun updateAccountStatus(newAccountStatus: AccountStatus) {
        _accountStatus.value = newAccountStatus
    }

    // MutableLiveData for error messages
    private var _errorMessage = MutableLiveData<String?>()
    var errorMessage: LiveData<String?> = _errorMessage


    // Set an error message
    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    // Function to validate the email format
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    fun generateUniqueSixDigitId(): Int {
        val uuid = UUID.randomUUID().toString()
        val hash = uuid.hashCode()  // Get the hash code of the UUID
        val positiveHash = Math.abs(hash) // Ensure the hash is positive
        return (positiveHash % 900000) + 100000 // Make sure the result is in the 6-digit range
    }

    fun generateCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Function to clear all input fields
    private fun clearFields() {
        _username.value = ""
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun initializeAdmin() {
        viewModelScope.launch {
            userRepository.initializeAdminUser()
        }
    }

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    // Display all user
    fun displayAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().observeForever { userList ->
                val sortedUsers = userList.sortedByDescending { user ->
                    dateFormat.parse(user.createdDate)?.time ?: 0L
                }
                _users.value = sortedUsers
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Register user function
    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        clearErrorMessage()
        when {
            username.value.isBlank() -> setErrorMessage("Username is required")
            email.value.isBlank() -> setErrorMessage("Email is required")
            password.value.isBlank() -> setErrorMessage("Password is required")
            confirmPassword.value.isBlank() -> setErrorMessage("Confirm Password is required")
            !isValidEmail(email.value) -> setErrorMessage("Invalid email format")
            password.value != confirmPassword.value -> setErrorMessage("Passwords do not match")
            else -> {
                setErrorMessage("") // Clear previous error messages

                viewModelScope.launch {
                    try {
                        Log.d("UserRegistration", "Starting user registration")
                        val userId = generateUniqueSixDigitId()
                        val currentDate = generateCurrentDate()
                        val user = User(
                            userID = userId,
                            userName = username.value,
                            email = email.value,
                            password = password.value,
                            profilePicture = "",
                            createdDate = currentDate,
                            accountStatus = AccountStatus.ACTIVE,
                            phoneNo = "",
                            birthDate = null,
                            role = UserRole.NORMAL
                        )
                        userRepository.registerUser(user) // Register user in the repository
                        clearFields()
                        onSuccess() // Handle successful registration
                    } catch (e: Exception) {
                        onError(e.message ?: "Registration failed") // Handle error
                    }
                }
            }
        }
    }

    private val _registerError = MutableLiveData<String?>()
    val registerError: LiveData<String?> = _registerError

    fun validateCredentials(username: String, email: String, password: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _registerError.value = "Please fill up all the fields"
            return false
        } else {
            _registerError.value = null
            return true
        }
    }


    // Admin add users
    fun adminAddUser(onSuccess: () -> Unit) {
        val userRole = UserRole.valueOf(role.value.name.uppercase())
        val accountStatus = AccountStatus.valueOf(accountStatus.value.name.uppercase())

        viewModelScope.launch {
            try {
                val currentDate = generateCurrentDate()
                val user = User(
                    userID = generateUniqueSixDigitId(),
                    userName = username.value,
                    email = email.value,
                    password = password.value,
                    profilePicture = "",
                    createdDate = currentDate,
                    accountStatus = accountStatus,
                    phoneNo = "",
                    birthDate = null,
                    role = userRole
                )
                userRepository.registerUser(user)
                clearFields()
                // Update the list of users
                displayAllUsers()
                onSuccess()
            } catch (e: Exception) {
                "Failed to add user: ${e.message}"
            }
        }
    }

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }

    //Login function
    fun loginUser(onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        clearErrorMessage()
        if (username.value.isBlank() || password.value.isBlank()) {
            setErrorMessage("Username and password cannot be empty")
            onError("Username and password cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                val user = userRepository.loginUser(username.value, password.value)
                if (user != null) {
                    clearFields()
                    setCurrentUser(user)
                    onSuccess(user)
                } else {
                    setErrorMessage("Invalid credentials")
                    onError("Invalid credentials")
                }
            } catch (e: Exception) {
                clearFields()
                val errorMsg = when (e) {
                    is AuthenticationException -> "Invalid username or password"
                    else -> "Login failed: ${e.message}"
                }
                setErrorMessage(errorMsg)
                onError(errorMsg)
            }
        }
    }


    // Update user
    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                displayAllUsers()
                _currentUser.value = user
                updateUsername("")
                updateEmail("")
                updatePassword("")
                updateRole(UserRole.NORMAL)
                updateAccountStatus(AccountStatus.ACTIVE)

            } catch (e: Exception) {
                "Failed to update user: ${e.message}"
            }
        }
    }


    fun handleProfilePictureUpload(uri: Uri) {
        viewModelScope.launch {
            val currentUser = _currentUser.value
            currentUser?.let {
                val imagePath = userRepository.saveImageToInternalStorage(it, uri)
                if (imagePath != null) {
                    val updatedUser = it.copy(profilePicture = imagePath)
                    userRepository.updateUser(updatedUser)
                    _currentUser.value = userRepository.getUserById(updatedUser.userID)
                }
            }
        }
    }

    private val _phoneValidationError = MutableLiveData<String?>()
    val phoneValidationError: LiveData<String?> = _phoneValidationError

    private val _emailValidationError = MutableLiveData<String?>()
    val emailValidationError: LiveData<String?> = _emailValidationError

    fun validatePhone(phone: String): Boolean {
        return if (phone.matches(Regex("^[0-9]{10}$"))) {
            _phoneValidationError.value = null
            true
        } else {
            _phoneValidationError.value = "Phone number must be exactly 10 digits long"
            false
        }
    }

    fun validateEmail(email: String): Boolean {
        return if (email.matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w+$"))) {
            _emailValidationError.value = null
            true
        } else {
            _emailValidationError.value = "Invalid email format"
            false
        }
    }

    // Delete user
    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(userId)
                displayAllUsers()
            } catch (e: Exception) {
                ("Failed to delete user: ${e.message}")
            }
        }
    }


    // Forgot Password Logic
    private val _forgotPasswordState = MutableLiveData(ForgotPasswordState.IDLE)
    val forgotPasswordState: LiveData<ForgotPasswordState> get() = _forgotPasswordState

    fun setForgotPasswordState(newState: ForgotPasswordState) {
        _forgotPasswordState.value = newState
    }

    private var _actualVerificationCode = mutableStateOf("")
    val actualVerificationCode: State<String> = _actualVerificationCode

    private var _enteredVerificationCode = mutableStateOf("")
    val enteredVerificationCode: State<String> = _enteredVerificationCode

    fun updateEnteredVerificationCode(newCode: String) {
        _enteredVerificationCode.value = newCode
    }

    private val _codeValidationError = MutableLiveData<String?>()
    val codeValidationError: LiveData<String?> = _codeValidationError


    fun validateVerificationCode(): Boolean {
        return if (actualVerificationCode.value == enteredVerificationCode.value) {
            _codeValidationError.value = null
            _enteredVerificationCode.value = ""
            true
        } else {
            _codeValidationError.value = "Invalid verification code"
            false
        }
    }


    fun generateVerificationCode(): String {
        val code = (100000..999999).random().toString()
        _actualVerificationCode.value = code
        return code
    }

    fun sendVerificationEmail(context: Context, recipientEmail: String, verificationCode: String) {
        val subject = "Verification Code"
        val message = "Your verification code is: $verificationCode"

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail)) // Recipient's email
            putExtra(Intent.EXTRA_SUBJECT, subject) // Email subject
            putExtra(Intent.EXTRA_TEXT, message) // Email body
        }

        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Choose an email client"))
        } else {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    fun updateUserPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user != null) {
                val updatedUser = user.copy(password = newPassword)
                userRepository.updateUser(updatedUser)
                // Optionally update your LiveData representing the current user.
                _currentUser.postValue(updatedUser)
            } else {
                setErrorMessage("User not found")
            }
        }
    }
    fun validateEmailForVerification(email: String): Boolean {
        if (validateEmail(email)) {
            CoroutineScope(Dispatchers.IO).launch {
                if (userRepository.isEmailExist(email)) {
                    _emailValidationError.postValue(null)
                } else {
                    _emailValidationError.postValue("Email doesn't exist")
                }
            }
            return true
        } else {
            _emailValidationError.postValue("Invalid email format")
            return false
        }
    }

    private val _passwordValidationError = MutableLiveData<String?>()
    val passwordValidationError: LiveData<String?> = _passwordValidationError

    fun validatePassword(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            _passwordValidationError.value = null
            true
        } else {
            _passwordValidationError.value = "Passwords do not match"
            false
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.IDLE
        _email.value = ""
        _enteredVerificationCode.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _errorMessage.value = null
    }
}
