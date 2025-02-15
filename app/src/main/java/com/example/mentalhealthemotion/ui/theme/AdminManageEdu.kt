package com.example.mentalhealthemotion.ui.theme


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import coil.compose.AsyncImage
import com.example.mentalhealthemotion.Data.EduContent
import com.example.mentalhealthemotion.Data.EduContentViewModel
import com.example.mentalhealthemotion.R

@Composable
fun AdminEducationalLibrary(eduViewModel: EduContentViewModel, backClick:() -> Unit) {
    val contentList by eduViewModel.contents.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        eduViewModel.displayAllContents()
    }
    val context = LocalContext.current
    var showAddContentDialog by remember { mutableStateOf(false) }
    var selectedContentIdEdit by remember { mutableStateOf<Int?>(null) }
    var selectedContentId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(120.dp)
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back icon",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { backClick() },
                    tint =  Color(0xFF2E3E64)
                )
                Text(
                    text = "Educational Library's Content",
                    fontSize = 25.sp,
                    color = Color(0xFF2E3E64),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 30.dp)
                )
            }


            // List of library items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(contentList) { index, content ->
                    val cardColor = if (index % 2 == 0) {
                        Color(0xFFBEE4F4)
                    } else {
                        Color(0xFFA7C7E7)
                    }
                    LibraryItem(
                        title = content.contentTitle,
                        description = content.contentDescription,
                        resourceUrl = content.resourceUrl,
                        imageUrl = content.imageUrl?: "No image selected",
                        cardColor = cardColor,
                        onEdit = { selectedContentIdEdit = content.contentID },
                        onDelete = { selectedContentId = content.contentID },
                        openUrl = {eduViewModel.openUrl(context = context, content.resourceUrl)}
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {showAddContentDialog = true},
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

    // Add content dialog
    if (showAddContentDialog) {
        AddNewContentDialog(
            eduViewModel = eduViewModel,
            onDismiss = {showAddContentDialog = false}
        )
    }

    // Update content dialog
    if (selectedContentIdEdit != null) {
        val content = contentList.find { it.contentID == selectedContentIdEdit }
        if (content != null) {
            EditContentDialog(
                eduViewModel = eduViewModel,
                content = content,
                onDismiss = {selectedContentIdEdit = null}
            )
        }
    }

    // Delete content dialog
    if (selectedContentId != null) {
        val content = contentList.find { it.contentID == selectedContentId }
        if (content != null) {
            DeleteContentConfirmationDialog(
                eduViewModel = eduViewModel,
                title = content.contentTitle,
                contentID = content.contentID,
                onDismiss = {selectedContentId = null}
            )
        }
    }
}

@Composable
fun LibraryItem(
    title: String,
    imageUrl: String,
    description: String,
    resourceUrl: String,
    cardColor: Color,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    openUrl: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Title: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(title)
                    }
                },
                fontSize = 16.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("Resource URL: ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Normal,
                            textDecoration = TextDecoration.Underline,
                            color = Color.Blue
                        )
                    ) { append(resourceUrl) }
                },
                fontSize = 16.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { openUrl() }
            )

            Spacer(modifier = Modifier.height(16.dp))
                // Decorated Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Text
                Text(
                    text = buildAnnotatedString {
                        append("Description:\n ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                            append(description)
                        }
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF2E3E64),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { onEdit() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Content",
                        tint = Color(0xFF2E3E64)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Content",
                        tint = Color(0xFF2E3E64)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(7.dp))
}


@Composable
fun ContentDialog(
    eduViewModel: EduContentViewModel,
    onDismiss: () -> Unit,
    urlError: String?,
    onAction: () -> Unit,
    title: String,
) {
    val images by eduViewModel.images.observeAsState(emptyList())
    val selectedImage by eduViewModel.selectedImage.observeAsState()

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
                    value = eduViewModel.title.value,
                    onValueChange = { eduViewModel.updateTitle(it) },
                    label = { Text("Content Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = eduViewModel.description.value,
                    onValueChange = { eduViewModel.updateDescription(it) },
                    label = { Text("Content Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = eduViewModel.resourceUrl.value,
                    onValueChange = { eduViewModel.updateResourceUrl(it)},
                    label = { Text("Resource URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Search field for images
                OutlinedTextField(
                    value = eduViewModel.keyword.value,
                    onValueChange = { eduViewModel.updateKeyword(it) },
                    label = { Text("Search Image") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )

                )

                Button(
                    onClick = { eduViewModel.searchImages(eduViewModel.keyword.value) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Search")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Image selection UI
                LazyRow {
                    items(images) { imageUrl ->
                        val imageUrlString = imageUrl.toString()
                        ImageCard(imageUrlString, selectedImage == imageUrlString) {
                            eduViewModel.updateSelectedImage(imageUrlString)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (urlError != null) {
                    Text(
                        text = urlError,
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
                        // Reset the state before dismissing the dialog
                        eduViewModel.updateTitle("")
                        eduViewModel.updateDescription("")
                        eduViewModel.updateResourceUrl("")
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
fun ImageCard(imageUrl: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(120.dp)
            .border(
                2.dp,
                if (isSelected) Color.Blue else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun AddNewContentDialog(eduViewModel: EduContentViewModel, onDismiss: () -> Unit) {
    val urlError by eduViewModel.urlError.observeAsState()
    val context = LocalContext.current

    ContentDialog(
        eduViewModel = eduViewModel,
        onDismiss = onDismiss,
        urlError = urlError,
        onAction = {
            if (eduViewModel.isValidUrl(eduViewModel.resourceUrl.value ?: "")) {
                eduViewModel.insertContent(onSuccess = { onDismiss() })
            } else {
                Toast.makeText(context, eduViewModel.urlError.value ?: "Invalid URL", Toast.LENGTH_SHORT).show()
            }
        },
        title = "Add New Content"
    )
}


@Composable
fun EditContentDialog(
    eduViewModel: EduContentViewModel,
    content: EduContent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val urlError by eduViewModel.urlError.observeAsState()
    LaunchedEffect(content) {
        eduViewModel.apply {
            updateTitle(content.contentTitle)
            updateDescription(content.contentDescription)
            updateResourceUrl(content.resourceUrl)
            updateSelectedImage(content.imageUrl)
        }
    }

    ContentDialog(
        eduViewModel = eduViewModel,
        onDismiss = onDismiss,
        urlError = urlError,
        onAction = {
            if (eduViewModel.isValidUrl(eduViewModel.resourceUrl.value ?: "")) {
                eduViewModel.updateContent(content.copy(
                    contentTitle = eduViewModel.title.value,
                    contentDescription = eduViewModel.description.value,
                    resourceUrl = eduViewModel.resourceUrl.value,
                    imageUrl = eduViewModel.selectedImage.value
                ))
                onDismiss()
            } else {
                Toast.makeText(context, eduViewModel.urlError.value ?: "Invalid URL", Toast.LENGTH_SHORT).show()
            }
        },
        title = "Edit Edu Content"
    )
}


@Composable
fun DeleteContentConfirmationDialog(
    eduViewModel: EduContentViewModel,
    title: String,
    contentID: Int,
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
                "Are you sure you want to delete content '${title} '?",
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
                        eduViewModel.deleteContent(contentID)
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

