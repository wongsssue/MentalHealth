package com.example.mentalhealthemotion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.mentalhealthemotion.Data.AccountStatus
import com.example.mentalhealthemotion.Data.User
import com.example.mentalhealthemotion.Data.UserRole
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R



@Composable
fun AdminUserList(userViewModel: UserViewModel, backClick: () -> Unit) {
    val users by userViewModel.users.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        userViewModel.displayAllUsers()
    }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserIdEdit by remember { mutableStateOf<Int?>(null) }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(90.dp)
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back icon",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { backClick() },
                    tint =  Color(0xFF2E3E64)
                )
                Spacer(modifier = Modifier.width(80.dp))
                Text(
                    text = "Users List",
                    fontSize = 29.sp,
                    color = Color(0xFF2E3E64),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 30.dp)
                )
            }

            // Display users list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(users) { index, user ->
                    val cardColor = if (index % 2 == 0) {
                        Color(0xFFBEE4F4)
                    } else {
                        Color(0xFFA7C7E7)
                    }

                    UserItem(
                        name = user.userName,
                        id = user.userID,
                        cardColor = cardColor,
                        onEdit = {
                            selectedUserIdEdit = user.userID
                        },
                        onDelete = {
                            selectedUserId = user.userID
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            backgroundColor = Color(0xFF2E3E64),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Content"
            )
        }
    }

    // Add user dialog
    if (showAddUserDialog) {
        AddUserDialog(
            userViewModel = userViewModel,
            onDismiss = { showAddUserDialog = false }
        )
    }

    // Update user dialog
    if (selectedUserIdEdit != null) {
        val user = users.find { it.userID == selectedUserIdEdit }
        if (user != null) {
            EditUserDialog(
                userViewModel = userViewModel,
                user = user,
                onDismiss = { selectedUserIdEdit = null }
            )
        }
    }

    // Delete user dialog
    if (selectedUserId != null) {
        val user = users.find { it.userID == selectedUserId }
        if (user != null) {
            DeleteConfirmationDialog(
                userViewModel = userViewModel,
                userName = user.userName,
                userId = user.userID,
                onDismiss = { selectedUserId = null }
            )
        }
    }
}


@Composable
fun UserItem(
    name: String,
    id: Int,
    cardColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_username),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "(id: $id)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit User",
                        tint = Color(0xFF2E3E64)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = Color(0xFF2E3E64)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(7.dp))
}

@Composable
fun UserDialog(
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    title: String,
    emailError: String?,
    registerError:String?
) {
    val roles = listOf(UserRole.ADMIN, UserRole.NORMAL).map { it.name } // Convert to String
    val statusOptions =
        listOf(AccountStatus.ACTIVE, AccountStatus.INACTIVE, AccountStatus.BANNED).map { it.name }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E3E64),
                modifier = Modifier.padding(bottom = 20.dp)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = userViewModel.username.value,
                    onValueChange = { userViewModel.updateUsername(it) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userViewModel.email.value,
                    onValueChange = { userViewModel.updateEmail(it) },
                    label = { Text("Email") },
                    isError = emailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(
                        text = emailError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userViewModel.password.value,
                    onValueChange = { userViewModel.updatePassword(it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(13.dp))
                DropdownMenu(
                    items = roles,
                    selectedItem = userViewModel.role.value.name,
                    onItemSelected = { userViewModel.updateRole(UserRole.valueOf(it.uppercase())) },
                    label = "User Role"
                )
                Spacer(modifier = Modifier.height(13.dp))

                DropdownMenu(
                    items = statusOptions,
                    selectedItem = userViewModel.accountStatus.value.name,
                    onItemSelected = { userViewModel.updateAccountStatus(AccountStatus.valueOf(it.uppercase())) },
                    label = "User Account Status"
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (registerError != null) {
                    Text(
                        text = registerError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        onAction()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E3E64))
                ) {
                    Text("Save", color = Color.White)
                }
                Button(
                    onClick = {
                        // Reset the user state before dismissing the dialog
                        userViewModel.updateUsername("")
                        userViewModel.updateEmail("")
                        userViewModel.updatePassword("")
                        userViewModel.updateRole(UserRole.NORMAL)
                        userViewModel.updateAccountStatus(AccountStatus.ACTIVE)
                        // Dismiss the dialog
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE0E0E0))
                ) {
                    Text("Cancel", color = Color.Black)
                }
            }
        },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun DropdownMenu(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selectedItem.ifEmpty { label },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    content = { Text(text = item) }
                )
            }
        }
    }
}


@Composable
fun AddUserDialog(userViewModel: UserViewModel, onDismiss: () -> Unit) {
    val registerError by userViewModel.registerError.observeAsState()
    val emailError by userViewModel.emailValidationError.observeAsState()
    val context = LocalContext.current
    UserDialog(
        userViewModel = userViewModel,
        onDismiss = onDismiss,
        emailError = emailError,
        registerError = registerError,
        onAction = {
            if (userViewModel.validateEmail(userViewModel.email.value)) {
                if (userViewModel.validateCredentials(
                        username = userViewModel.username.value,
                        email = userViewModel.email.value,
                        password = userViewModel.password.value
                    )
                ) {
                    userViewModel.adminAddUser(
                        onSuccess = {
                            onDismiss()
                        }
                    )
                } else {
                    Toast.makeText(context, userViewModel.registerError.value, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, userViewModel.emailValidationError.value, Toast.LENGTH_SHORT).show()
            }
        },
        title = "Add New User"
    )
}

@Composable
fun EditUserDialog(
    userViewModel: UserViewModel,
    user: User,
    onDismiss: () -> Unit
) {
    val registerError by userViewModel.registerError.observeAsState()
    val emailError by userViewModel.emailValidationError.observeAsState()
    val context = LocalContext.current

    // Load user data into the ViewModel on dialog launch
    LaunchedEffect(user) {
        userViewModel.apply {
            updateUsername(user.userName)
            updateEmail(user.email)
            updatePassword(user.password)
            updateRole(user.role)
            updateAccountStatus(user.accountStatus)
        }
    }

    UserDialog(
        userViewModel = userViewModel,
        emailError = emailError,
        registerError = registerError,
        onDismiss = onDismiss,
        onAction = {
            if (userViewModel.validateEmail(userViewModel.email.value)) {
                if (userViewModel.validateCredentials(
                        username = userViewModel.username.value,
                        email = userViewModel.email.value,
                        password = userViewModel.password.value
                    )
                ) {
                    userViewModel.updateUser(
                        user.copy(
                            userName = userViewModel.username.value,
                            email = userViewModel.email.value,
                            password = userViewModel.password.value,
                            role = userViewModel.role.value,
                            accountStatus = userViewModel.accountStatus.value
                        )
                    )
                    onDismiss()
                } else {
                    Toast.makeText(context, userViewModel.registerError.value, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, userViewModel.emailValidationError.value, Toast.LENGTH_SHORT).show()
            }
        },
        title = "Edit User"
    )
}


@Composable
fun DeleteConfirmationDialog(
    userViewModel: UserViewModel,
    userName: String,
    userId:Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confirm Deletion",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E3E64)
            )
        },
        text = {
            Text(
                "Are you sure you want to delete user '${userName} with user id of ${userId}'?",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3E64),
            )
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        userViewModel.deleteUser(userId)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF2E3E64),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Text("Delete")
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(16.dp)
    )
}