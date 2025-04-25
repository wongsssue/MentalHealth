package com.example.mentalhealthemotion.Data

import kotlin.random.Random

class WordSearchRepository {

    private val wordList = listOf(
        "CALM", "FOCUS", "RELAX", "MINDFUL", "PEACE",
        "BREATHE", "BALANCE", "HEALTH", "STRESS", "HAPPY", "POSITIVE", "COURAGE",
        "HOPE", "GRATITUDE", "JOY", "LOVE", "STRENGTH", "CONFIDENCE", "HARMONY",
        "SERENITY", "BRAVE", "MOTIVATED", "CHEERFUL", "PATIENCE", "AWARENESS", "KINDNESS",
        "ENERGY", "FORGIVENESS", "VIBRANT", "BLOOM", "ZEN", "INSPIRE", "PEACEFUL"
    )

    fun generatePuzzle(gridSize: Int = 8): WordSearchPuzzle {
        val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }

        val selectedWords = mutableListOf<String>()

        // Pick random words and place them in the grid
        wordList.shuffled().take(5).forEach { word ->
            if (placeWordInGrid(grid, word)) {
                selectedWords.add(word) // Add word to list only if it was successfully placed
            }
        }

        // Fill empty spaces with random letters
        for (i in grid.indices) {
            for (j in grid[i].indices) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = ('A'..'Z').random()
                }
            }
        }

        return WordSearchPuzzle(grid.map { it.toList() }, selectedWords)
    }

    private fun placeWordInGrid(grid: Array<CharArray>, word: String): Boolean {
        val gridSize = grid.size
        val random = Random.Default
        val direction = random.nextInt(4) // 0: horizontal, 1: vertical, 2: diagonal down, 3: diagonal up

        var placed = false
        var row: Int
        var col: Int
        var attempts = 0
        val maxAttempts = 100 // Limit the number of attempts to avoid infinite loop

        while (!placed && attempts < maxAttempts) {
            row = random.nextInt(gridSize)
            col = random.nextInt(gridSize)

            when (direction) {
                0 -> { // Horizontal
                    if (col + word.length <= gridSize && canPlaceWordHorizontally(grid, word, row, col)) {
                        for (i in word.indices) grid[row][col + i] = word[i]
                        placed = true
                    }
                }
                1 -> { // Vertical
                    if (row + word.length <= gridSize && canPlaceWordVertically(grid, word, row, col)) {
                        for (i in word.indices) grid[row + i][col] = word[i]
                        placed = true
                    }
                }
                2 -> { // Diagonal Down
                    if (row + word.length <= gridSize && col + word.length <= gridSize && canPlaceWordDiagonalDown(grid, word, row, col)) {
                        for (i in word.indices) grid[row + i][col + i] = word[i]
                        placed = true
                    }
                }
                3 -> { // Diagonal Up
                    if (row - word.length >= 0 && col + word.length <= gridSize && canPlaceWordDiagonalUp(grid, word, row, col)) {
                        for (i in word.indices) grid[row - i][col + i] = word[i]
                        placed = true
                    }
                }
            }
            attempts++
        }

        return placed
    }


    private fun canPlaceWordHorizontally(grid: Array<CharArray>, word: String, row: Int, col: Int): Boolean {
        for (i in word.indices) {
            if (grid[row][col + i] != ' ' && grid[row][col + i] != word[i]) return false
        }
        return true
    }

    private fun canPlaceWordVertically(grid: Array<CharArray>, word: String, row: Int, col: Int): Boolean {
        for (i in word.indices) {
            if (grid[row + i][col] != ' ' && grid[row + i][col] != word[i]) return false
        }
        return true
    }

    private fun canPlaceWordDiagonalDown(grid: Array<CharArray>, word: String, row: Int, col: Int): Boolean {
        for (i in word.indices) {
            if (grid[row + i][col + i] != ' ' && grid[row + i][col + i] != word[i]) return false
        }
        return true
    }

    private fun canPlaceWordDiagonalUp(grid: Array<CharArray>, word: String, row: Int, col: Int): Boolean {
        for (i in word.indices) {
            if (grid[row - i][col + i] != ' ' && grid[row - i][col + i] != word[i]) return false
        }
        return true
    }
}
