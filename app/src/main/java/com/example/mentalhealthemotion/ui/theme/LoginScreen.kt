package com.example.mentalhealthemotion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.ForgotPasswordState
import com.example.mentalhealthemotion.Data.UserRole
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R


@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    navigateToAdminPage: () -> Unit,
    navigateToUserPage: () -> Unit,
    navigateToRegisterPage: () -> Unit
) {
    val forgotPasswordState by userViewModel.forgotPasswordState.observeAsState(ForgotPasswordState.IDLE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3E5FC)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.backgroundimage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (forgotPasswordState) {
                ForgotPasswordState.IDLE -> {
                    LoginTextFields(
                        userViewModel = userViewModel,
                        navigateToAdminPage = navigateToAdminPage,
                        navigateToUserPage = navigateToUserPage,
                        navigateToRegisterPage = navigateToRegisterPage
                    )
                }
                ForgotPasswordState.ACTIVATED -> {
                    ForgotPasswordStateActivated(userViewModel)
                }
                ForgotPasswordState.EMAIL_VALID -> {
                    ForgotPasswordStateValidEmail(userViewModel)
                }
                ForgotPasswordState.CODE_VALID -> {
                    ForgotPasswordStateValidCode(userViewModel)
                }
            }
        }
    }
}

@Composable
fun LoginTextFields(
    userViewModel: UserViewModel,
    navigateToAdminPage: () -> Unit,
    navigateToUserPage: () -> Unit,
    navigateToRegisterPage: () -> Unit
) {
    val errorMessage by userViewModel.errorMessage.observeAsState()

    Text(
        text = "Welcome Back",
        fontSize = 40.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Login to your account",
        fontSize = 20.sp,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(10.dp),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    TextFieldRow(
        placeholder = "Username",
        onValueChange = { userViewModel.updateUsername(it) },
        icon = R.drawable.ic_username,
        imeAction = ImeAction.Next,
        value = userViewModel.username.value,
        visualTransformation = VisualTransformation.None
    )
    TextFieldRow(
        placeholder = "Password",
        onValueChange = { userViewModel.updatePassword(it) },
        keyboardType = KeyboardType.Password,
        icon = R.drawable.ic_password,
        imeAction = ImeAction.Done,
        value = userViewModel.password.value,
        visualTransformation = PasswordVisualTransformation()
    )
    Spacer(modifier = Modifier.height(30.dp))
    TextButton(
        onClick = { userViewModel.setForgotPasswordState(ForgotPasswordState.ACTIVATED) }
    ) {
        Text(
            text = "Forgot Password?",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = {
            userViewModel.loginUser(
                onSuccess = { user ->
                    when (user.role) {
                        UserRole.ADMIN -> navigateToAdminPage()
                        UserRole.NORMAL -> navigateToUserPage()
                        else -> userViewModel.setErrorMessage("Unknown role")
                    }
                },
                onError = { errorMessage ->
                    userViewModel.setErrorMessage(errorMessage)
                }
            )
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .height(50.dp)
            .width(200.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
    ) {
        Text(text = "LOGIN", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(10.dp))
    errorMessage?.let {
        Text(
            text = it,
            color = Color.Red,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
    TextButton(onClick = navigateToRegisterPage) {
        Text(
            text = "Don't have an account? Sign up",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Composable
fun ForgotPasswordStateActivated(userViewModel: UserViewModel) {
    val emailError by userViewModel.emailValidationError.observeAsState()
    val context = LocalContext.current

    Text(
        text = "Enter your email for verification",
        fontSize = 20.sp,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(10.dp),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    TextFieldRow(
        icon = R.drawable.ic_email,
        value = userViewModel.email.value,
        onValueChange = { userViewModel.updateEmail(it) },
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
        placeholder = "Enter your email",
        visualTransformation = VisualTransformation.None
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
        if (userViewModel.validateEmailForVerification(userViewModel.email.value)) {
            userViewModel.sendVerificationEmail(context, userViewModel.email.value, userViewModel.generateVerificationCode())
            userViewModel.setForgotPasswordState(ForgotPasswordState.EMAIL_VALID)
        } else {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
        }
    },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .height(50.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
    ) {
        Text("Send Verification Code",color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(12.dp))
    TextButton(
        onClick = { userViewModel.setForgotPasswordState(ForgotPasswordState.IDLE) },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            "Cancel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textDecoration = TextDecoration.Underline
        )
    }

}

@Composable
fun ForgotPasswordStateValidEmail(userViewModel: UserViewModel) {
    val codeError by userViewModel.codeValidationError.observeAsState()
    val context = LocalContext.current
    Text(
        text = "Enter the 6-digit code sent to your email",
        fontSize = 20.sp,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(10.dp),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    TextFieldRow(
        icon = R.drawable.code,
        value = userViewModel.enteredVerificationCode.value,
        onValueChange = { userViewModel.updateEnteredVerificationCode(it) },
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
        placeholder = "Enter verification code",
        visualTransformation = VisualTransformation.None
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
        if (userViewModel.validateVerificationCode()) {
            userViewModel.setForgotPasswordState(ForgotPasswordState.CODE_VALID)
        } else {
            Toast.makeText(context, codeError ?: "Invalid code", Toast.LENGTH_SHORT).show()
        }
    },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .height(50.dp)
            .width(200.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
    ) {
        Text("Verify Code",color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForgotPasswordStateValidCode(userViewModel: UserViewModel) {
    val passwordError by userViewModel.passwordValidationError.observeAsState()
    val context = LocalContext.current
    val user by userViewModel.currentUser.observeAsState()
    Text(
        text = "Reset your password",
        fontSize = 20.sp,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(10.dp),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    TextFieldRow(
        imeAction = ImeAction.Next,
        placeholder = "New Password",
        onValueChange = { userViewModel.updatePassword(it) },
        keyboardType = KeyboardType.Password,
        icon = R.drawable.ic_password,
        value = userViewModel.password.value,
        visualTransformation = PasswordVisualTransformation()
    )
    TextFieldRow(
        imeAction = ImeAction.Done,
        placeholder = "Confirm new password",
        onValueChange = { userViewModel.updateConfirmPassword(it) },
        keyboardType = KeyboardType.Password,
        icon = R.drawable.ic_password,
        value = userViewModel.confirmPassword.value,
        visualTransformation = PasswordVisualTransformation()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
        if(userViewModel.validatePassword(userViewModel.password.value, userViewModel.confirmPassword.value)) {
            userViewModel.updateUserPassword(userViewModel.email.value, userViewModel.password.value)
            userViewModel.resetForgotPasswordState()
        }
        else{
            Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
        }
    },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .height(50.dp)
            .width(200.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
    ) {
        Text("Update Password",color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}


