package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mentalhealthemotion.Data.EduContentViewModel


@Composable
fun EducationalLibraryPage(eduViewModel: EduContentViewModel, onNavigate: (String) -> Unit) {
    val contentList by eduViewModel.contents.observeAsState(emptyList())
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        eduViewModel.displayAllContents()
    }

    val searchText = remember { mutableStateOf("") }
    val filteredItems = contentList.filter {
        it.contentTitle.contains(searchText.value, ignoreCase = true) ||
                it.contentDescription.contains(searchText.value, ignoreCase = true)
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Educational Library",
                fontSize = 30.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 30.dp)
            )
            // Search Bar
            TextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                placeholder = {
                    Text(
                        text = "Search Keywords",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    ) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(35.dp))
            LazyColumn {
                itemsIndexed(filteredItems) { index, item ->
                    LibraryItem(
                        title = item.contentTitle,
                        imageUrl = item.imageUrl ?: "No image selected",
                        description = item.contentDescription,
                        resourceUrl = item.resourceUrl,
                        openUrl = { eduViewModel.openUrl(context = context, item.resourceUrl) },
                        isLastItem = index == filteredItems.lastIndex
                    )
                }
            }
        }
        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun LibraryItem(
    title: String,
    imageUrl: String,
    description: String,
    resourceUrl: String,
    openUrl: () -> Unit,
    isLastItem: Boolean = false
) {
    Column(
        modifier = Modifier.padding(bottom = if (isLastItem) 100.dp else 15.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Image
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Library Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    // Feathered Border (Gradient Overlay)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.White),
                                    center = Offset.Unspecified,
                                    radius = 750f
                                )
                            )
                    )

                    // Title Overlay with Gradient Text and Shadow
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black, // 3D Shadow Effect
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(12.dp)
                    )
                }

                // Description Text
                Text(
                    text = description,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E3E64),
                    modifier = Modifier.padding(16.dp)
                )

                // URL
                Text(
                    text = buildAnnotatedString {
                        append("Feel free to explore more at: \n ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.Underline,
                                color = Color.Blue
                            )
                        ) {
                            append(resourceUrl)
                        }
                    },
                    fontSize = 15.sp,
                    color = Color(0xFF2E3E64),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { openUrl() }
                )
            }
        }
    }
}
