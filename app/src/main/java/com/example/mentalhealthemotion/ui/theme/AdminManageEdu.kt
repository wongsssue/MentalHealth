package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.mentalhealthemotion.R

@Composable
fun AdminEducationalLibrary(backClick:() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                    .size(35.dp)
                    .clickable { backClick() }
            )
            Text(
                text = "Educational Library's Content",
                fontSize = 30.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }


        // List of library items
        val libraryItems = listOf(
            "5 Emotion Regulation Skills You Should Master",
            "Emotion Regulation Skills",
            "Mental Health Awareness",
            "Overcoming Anxiety",
            "Effective Stress Management"
        )

        libraryItems.forEachIndexed { index, title ->
            val cardColor = if (index % 2 == 0) {
                Color(0xFFBEE4F4)
            } else {
                Color(0xFFA7C7E7)
            }
            LibraryItem(title = title, cardColor = cardColor) {}
        }

        Spacer(modifier = Modifier.weight(1f))
        FloatingActionButton(
            onClick = { /* Add Content */},
            backgroundColor = Color(0xFF2E3E64),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Content"
            )
        }
    }
}

@Composable
fun LibraryItem(
    title: String,
    cardColor: Color,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            Row {
                IconButton(onClick = { onEdit() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Content",
                        tint =  Color(0xFF2E3E64)
                    )
                }

                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Content",
                        tint =  Color(0xFF2E3E64)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(7.dp))
}

@Preview(showBackground = true)
@Composable
fun PreviewEdu() {
    AdminEducationalLibrary(backClick = {})
}

