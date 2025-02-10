package com.example.mentalhealthemotion.ui.theme

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mentalhealthemotion.Data.ProfileField
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R
import java.util.Calendar


@Composable
fun ProfilePage(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val user by userViewModel.currentUser.observeAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val profilePicture= selectedImageUri?.toString() ?: user?.profilePicture

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            userViewModel.handleProfilePictureUpload(it)
        }
    }

    val fields = remember(user) {
        listOf(
            ProfileField(R.drawable.ic_username, user?.userName ?: "-", "Your Name", onEdit = {}),
            ProfileField(R.drawable.ic_phone, user?.phoneNo ?: "-", "Contact Number", onEdit = {}),
            ProfileField(R.drawable.ic_email, user?.email ?: "-", "Email Address", onEdit = {}),
            ProfileField(R.drawable.ic_calendar, user?.birthDate ?: "-", "Birth Date", onEdit = {}),
            ProfileField(R.drawable.ic_password, user?.password ?: "-", "Password", onEdit = {})
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFBEE4F4))
                .padding(start = 20.dp, top = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() },
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
        }

        // Profile Picture Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(shape = RoundedCornerShape(bottomEnd = 160.dp, bottomStart = 160.dp))
                .background(Color(0xFFBEE4F4)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = -70.dp)
                    .shadow(10.dp, shape = CircleShape)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = if (selectedImageUri != null || !profilePicture.isNullOrEmpty()) {
                        rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri ?: profilePicture)
                                .diskCacheKey(profilePicture ?: selectedImageUri?.toString())
                                .build()
                        )
                    } else {
                        painterResource(id = R.drawable.ic_username)
                    },
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Profile Info Cards
        fields.forEach { field ->
            ProfileInfoCard(
                userViewModel,
                icon = field.icon,
                text = field.text,
                iconDescription = field.description,
                onEdit = { newValue ->
                    user?.let {
                        val updatedUser = when (field.description) {
                            "Your Name" -> it.copy(userName = newValue)
                            "Contact Number" -> it.copy(phoneNo = newValue)
                            "Email Address" -> it.copy(email = newValue)
                            "Birth Date" -> it.copy(birthDate = newValue)
                            "Password" -> it.copy(password = newValue)
                            else -> it
                        }
                        userViewModel.updateUser(updatedUser)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}


@Composable
fun ProfileInfoCard(
    userViewModel: UserViewModel,
    icon: Int,
    text: String,
    iconDescription: String,
    onEdit: (String) -> Unit,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(100.dp)
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = iconDescription,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = iconDescription,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.forward),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isDialogVisible = true }
            )
        }
    }
    if (isDialogVisible) {
        if (iconDescription != "Birth Date") {
            EditDialog(
                userViewModel = userViewModel,
                editLabel = "Edit $iconDescription",
                initialValue = text,
                onSave = { newValue ->
                    onEdit(newValue)
                    isDialogVisible = false
                },
                onCancel = { isDialogVisible = false }
            )
        } else {
            DatePicker(onDateSelected = { selectedDate ->
                onEdit(selectedDate)
                isDialogVisible = false
            })
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

}

@Composable
fun EditDialog(
    userViewModel: UserViewModel,
    editLabel: String,
    initialValue: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf(initialValue) }
    val phoneError by userViewModel.phoneValidationError.observeAsState()
    val emailError by userViewModel.emailValidationError.observeAsState()
    val context = LocalContext.current


    AlertDialog(
        onDismissRequest = { onCancel() },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if ((editLabel == "Edit Contact Number" && phoneError != null) ||
                    (editLabel == "Edit Email Address" && emailError != null)) {
                    Text(
                        text = when (editLabel) {
                            "Edit Contact Number" -> phoneError ?: ""
                            "Edit Email Address" -> emailError ?: ""
                            else -> ""
                        },
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { onCancel() },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }

                    TextButton(
                        onClick = {
                            when (editLabel) {
                                "Edit Contact Number" -> {
                                    if (userViewModel.validatePhone(textFieldValue)) {
                                        onSave(textFieldValue)
                                    } else {
                                        Toast.makeText(context, phoneError, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                "Edit Email Address" -> {
                                    if (userViewModel.validateEmail(textFieldValue)) {
                                        onSave(textFieldValue)
                                    } else {
                                        Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                else -> {
                                    onSave(textFieldValue)
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        title = {
            Text(
                editLabel,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E3E64),
                modifier = Modifier.padding(bottom = 20.dp)
            )
        },
        text = {
            Column {
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text("Enter value") },
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .background(Color.White)
                        .border(
                            width = 1.5.dp,
                            color = Color.LightGray
                        ),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun DatePicker(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = "$dayOfMonth/${month + 1}/$year"
            selectedDate.value = date
            onDateSelected(date)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
    Text(selectedDate.value)
}


