package com.example.mentalhealthemotion.ui.theme

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mentalhealthemotion.Data.WordSearchViewModel
import com.example.mentalhealthemotion.Data.WordSearchViewModelFactory
import com.example.mentalhealthemotion.Data.WordSearchRepository

@Composable
fun WordSearchScreen(wordRepository: WordSearchRepository) {
    val viewModel: WordSearchViewModel = viewModel(factory = WordSearchViewModelFactory(wordRepository))
    val puzzle by viewModel.puzzle.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val scaffoldState = rememberScaffoldState()

    // Show Snackbar for success message
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title for Word Search Puzzle
            Text(text = "Word Search Puzzle", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            puzzle?.let { puzzleData ->
                // WordSearch Grid
                WordSearchGrid(
                    grid = puzzleData.grid,
                    selectedCells = viewModel.selectedCells,
                    foundWords = viewModel.foundWords,
                    foundWordCoordinates = viewModel.foundWordCoordinates, // Pass found word coordinates here
                    onLetterClick = { row, col -> viewModel.handleLetterClick(row, col) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word List
                WordList(words = puzzleData.words, foundWords = viewModel.foundWords, grid = puzzleData.grid)
            }

            // Push the button to the bottom
            Spacer(modifier = Modifier.weight(1f))  // This will push the button down

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateNewPuzzle() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF5293F6),
                    contentColor = Color.White
                )
            ) {
                Text("Generate New Puzzle")
            }

            Text(
                text = "Need to CLICK the word from first to end instead of middle.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier.padding(8.dp)
            )


        }
    }
}



@Composable
fun WordSearchGrid(
    grid: List<List<Char>>,
    selectedCells: List<Pair<Int, Int>>,
    foundWords: List<String>,
    foundWordCoordinates: List<List<Pair<Int, Int>>>, // Pass the found word coordinates
    onLetterClick: (Int, Int) -> Unit
) {
    val gridSize = grid.size

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    ) {
        itemsIndexed(grid.flatten()) { index, letter ->
            val row = index / gridSize
            val col = index % gridSize

            // Check if this cell is part of any found word
            val isInFoundWord = foundWordCoordinates.any { wordCoordinates ->
                wordCoordinates.contains(row to col)
            }

            // Color logic for the found word, selected word, and normal cells
            val cellBackgroundColor = when {
                selectedCells.contains(row to col) -> Color.Yellow // Selected cells
                isInFoundWord -> Color.LightGray // Found word cells
                else -> Color.White // Normal cells
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color.Black)
                    .background(cellBackgroundColor)
                    .clickable { onLetterClick(row, col) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
fun WordList(words: List<String>, foundWords: List<String>, grid: List<List<Char>>) {
    Column {
        Text(text = "Find these words:", fontSize = 25.sp, fontWeight = FontWeight.Bold)

        // Only show words that actually exist in the grid, with bounds check
        val validWords = words.filter { word -> isWordInGrid(grid, word) }

        validWords.forEach { word ->
            Text(
                text = word,
                fontSize = 20.sp,
                color = if (foundWords.contains(word)) Color.Green else Color.Black
            )
        }
    }
}


fun isWordInGrid(grid: List<List<Char>>, word: String): Boolean {
    val gridSize = grid.size

    // Check horizontally
    for (row in grid.indices) {
        for (col in 0..gridSize - word.length) {
            if ((0 until word.length).all { grid[row][col + it] == word[it] }) {
                return true
            }
        }
    }

    // Check vertically
    for (col in grid.indices) {
        for (row in 0..gridSize - word.length) {
            if ((0 until word.length).all { grid[row + it][col] == word[it] }) {
                return true
            }
        }
    }

    // Check diagonally (down-right)
    for (row in 0..gridSize - word.length) {
        for (col in 0..gridSize - word.length) {
            if ((0 until word.length).all { grid[row + it][col + it] == word[it] }) {
                return true
            }
        }
    }

    // Check diagonally (up-right)
    for (row in word.length - 1 until gridSize) {
        for (col in 0..gridSize - word.length) {
            if ((0 until word.length).all { grid[row - it][col + it] == word[it] }) {
                return true
            }
        }
    }

    return false
}
