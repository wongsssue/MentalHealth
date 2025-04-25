package com.example.mentalhealthemotion.Data

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WordSearchViewModel(
    private val repository: WordSearchRepository
) : ViewModel() {

    private val _puzzle = MutableStateFlow<WordSearchPuzzle?>(null)
    val puzzle: StateFlow<WordSearchPuzzle?> = _puzzle

    private val _selectedCells = mutableStateListOf<Pair<Int, Int>>() // Track selected cells
    val selectedCells: List<Pair<Int, Int>> get() = _selectedCells

    private val _foundWords = mutableStateListOf<String>() // Track found words
    val foundWords: List<String> get() = _foundWords

    private val _foundWordCoordinates = mutableStateListOf<List<Pair<Int, Int>>>() // Track coordinates for found words
    val foundWordCoordinates: List<List<Pair<Int, Int>>> get() = _foundWordCoordinates

    private val _successMessage = MutableStateFlow<String?>(null) // Success message state
    val successMessage: StateFlow<String?> = _successMessage

    init {
        generateNewPuzzle()
    }

    // Generate a new puzzle
    fun generateNewPuzzle(gridSize: Int = 8) {
        viewModelScope.launch {
            _puzzle.value = repository.generatePuzzle(gridSize)
            _selectedCells.clear() // Clear previous selections
            _foundWords.clear() // Reset found words
            _foundWordCoordinates.clear() // Clear found word coordinates
            _successMessage.value = null // Reset message
        }
    }

    // Handle letter selection
    fun handleLetterClick(row: Int, col: Int) {
        val clickedCell = row to col

        // Toggle cell selection
        if (clickedCell in _selectedCells) {
            _selectedCells.remove(clickedCell)
        } else {
            _selectedCells.add(clickedCell)
        }

        // Check for word completion
        checkForWord()
    }

    // Check if a valid word is formed
    private fun checkForWord() {
        val puzzleData = _puzzle.value ?: return

        // Construct the selected word from the grid
        val selectedWord = _selectedCells.map { (row, col) -> puzzleData.grid[row][col] }.joinToString("")

        // If the selected word is valid and not already found
        if (selectedWord in puzzleData.words && selectedWord !in _foundWords) {
            _foundWords.add(selectedWord)
            _successMessage.value = "Great! You found: $selectedWord 🎉"

            // Store the coordinates of the found word
            val wordCoordinates = getWordCoordinates(selectedWord, _selectedCells)
            _foundWordCoordinates.add(wordCoordinates)

            playSuccessSound() // Play sound after finding a word
            _selectedCells.clear() // Clear selection after success
        }

        // Check if all words are found
        if (_foundWords.size == puzzleData.words.size) {
            _successMessage.value = "You found all words! Generating new puzzle..."
            playSuccessSound() // Play sound when all words are found
            generateNewPuzzle() // Generate a new puzzle automatically
        }
    }

    // Function to get coordinates of a word from the selected cells
    private fun getWordCoordinates(word: String, selectedCells: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val wordLength = word.length
        val coordinates = mutableListOf<Pair<Int, Int>>()

        for (i in 0 until wordLength) {
            coordinates.add(selectedCells[i])
        }

        return coordinates
    }

    // Play success sound (short beep)
    fun playSuccessSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 150) // Short beep for success
    }
}
