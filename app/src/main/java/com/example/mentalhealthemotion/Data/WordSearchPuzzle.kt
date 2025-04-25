package com.example.mentalhealthemotion.Data

data class WordSearchPuzzle(
    val grid: List<List<Char>>, // 2D grid of characters
    val words: List<String> // List of words to find
)