package com.example.mentalhealthemotion.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.mentalhealthemotion.R
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.example.mentalhealthemotion.Data.User
import com.example.mentalhealthemotion.Data.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun RegisterScreen(userViewModel: UserViewModel, loginPage: () -> Unit) {

    val errorMessage by userViewModel.errorMessage.observeAsState()

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
            Text(
                text = "Register",
                fontSize = 40.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Create your account",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            TextFieldRow(
                imeAction = ImeAction.Next,
                placeholder = "Username",
                onValueChange = { userViewModel.updateUsername(it)},
                icon = R.drawable.ic_username,
                value = userViewModel.username.value,
                visualTransformation = VisualTransformation.None
            )

            TextFieldRow(
                imeAction = ImeAction.Next,
                placeholder = "Email address",
                onValueChange = { userViewModel.updateEmail(it) },
                keyboardType = KeyboardType.Email,
                icon = R.drawable.ic_email,
                value = userViewModel.email.value,
                visualTransformation = VisualTransformation.None
            )

            TextFieldRow(
                imeAction = ImeAction.Next,
                placeholder = "Password",
                onValueChange = { userViewModel.updatePassword(it) },
                keyboardType = KeyboardType.Password,
                icon = R.drawable.ic_password,
                value = userViewModel.password.value,
                visualTransformation = PasswordVisualTransformation()
            )

            TextFieldRow(
                imeAction = ImeAction.Done,
                placeholder = "Confirm password",
                onValueChange = { userViewModel.updateConfirmPassword(it) },
                keyboardType = KeyboardType.Password,
                icon = R.drawable.ic_password,
                value = userViewModel.confirmPassword.value,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    userViewModel.registerUser(
                        onSuccess = {
                            loginPage()
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
                Text(text = "REGISTER", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { loginPage() }) {
                Text(
                    text = "Already have an account? Login",
                    color = Color.Black,
                    textDecoration = TextDecoration.Underline
                )
            }

            // Display error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TextFieldRow(
    imeAction: ImeAction,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    @DrawableRes icon: Int? = null,
    value: String,
    visualTransformation: VisualTransformation
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(0.5.dp, shape = RoundedCornerShape(8.dp)),
        leadingIcon = {
            icon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = "Icon",
                    tint = Color.Black
                )
            }
        },
        visualTransformation = visualTransformation
    )
}
