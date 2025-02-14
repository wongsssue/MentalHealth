package com.example.mentalhealthemotion.Data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EduContentViewModel(private val repository: EduContentRepository) : ViewModel() {

    private var _title = mutableStateOf("")
    private var _description = mutableStateOf("")
    private var _resourceUrl = mutableStateOf("")
    private var _keyword = mutableStateOf("")

    var title: State<String> = _title
    var description: State<String> = _description
    var resourceUrl: State<String> = _resourceUrl
    var keyword: State<String> = _keyword

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateResourceUrl(newUrl: String) {
        _resourceUrl.value = newUrl
    }

    fun updateKeyword(newKeyword: String) {
        _keyword.value = newKeyword
    }

    fun generateUniqueFiveDigitId(): Int {
        val uuid = UUID.randomUUID().toString()
        val hash = uuid.hashCode()
        val positiveHash = Math.abs(hash)
        return (positiveHash % 90000) + 10000
    }

    fun generateCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun clearFields() {
        _title.value = ""
        _description.value = ""
        _resourceUrl.value= ""
        _selectedImage.value = ""
        _keyword.value = ""
    }

    fun initializeContent() {
        viewModelScope.launch {
            repository.initDefaultContent()
        }
    }

    private val _urlError = MutableLiveData<String?>()
    val urlError: LiveData<String?> = _urlError

    fun isValidUrl(url: String): Boolean {
        return if (Patterns.WEB_URL.matcher(url).matches()) {
            _urlError.value = null
            true
        } else {
            _urlError.value = "Invalid URL format"
            false
        }
    }

    private val _contents = MutableLiveData<List<EduContent>>(emptyList())
    val contents: LiveData<List<EduContent>> = _contents

    fun displayAllContents() {
        viewModelScope.launch {
            repository.getAllContent().observeForever { contents ->
                _contents.value = contents
            }
        }
    }

    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> get() = _images

    private val _selectedImage = MutableLiveData<String?>()
    val selectedImage: LiveData<String?> get() = _selectedImage

    fun searchImages(keyword: String) {
        viewModelScope.launch {
            try {
                val fetchedImages = repository.fetchImagesFromPexels(keyword)

                if (fetchedImages.isNullOrEmpty()) {
                    Log.e("EduContentViewModel", "No images found for '$keyword'")
                    _images.postValue(emptyList())
                } else {
                    _images.postValue(fetchedImages)
                }
            } catch (e: Exception) {
                Log.e("EduContentViewModel", "Network error: ${e.message}")
                _images.postValue(emptyList())
            }
        }
    }


    fun updateSelectedImage(imageUrl: String?) {
        _selectedImage.postValue(imageUrl)
    }

    fun insertContent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try{
                val content = EduContent(
                    contentID = generateUniqueFiveDigitId(),
                    contentTitle = title.value,
                    contentDescription = description.value,
                    resourceUrl = resourceUrl.value,
                    imageUrl = selectedImage.value,
                    dateCreated = generateCurrentDate()
                )
                repository.insertContent(content)
                displayAllContents()
                clearFields()
                onSuccess()
            } catch (e: Exception){
                "Failed to add content: ${e.message}"
            }

        }
    }

    fun updateContent(eduContent: EduContent) {
        viewModelScope.launch {
            try {
                repository.updateContent(eduContent)
                displayAllContents()
                updateTitle("")
                updateDescription("")
            } catch (e: Exception) {
                "Failed to update content: ${e.message}"
            }
        }
    }

    fun deleteContent(contentID: Int) {
        viewModelScope.launch {
            try {
                repository.deleteContent(contentID)
                displayAllContents()
            } catch (e: Exception) {
                ("Failed to delete content: ${e.message}")
            }
        }
    }

    fun getContentById(contentID: Int): LiveData<EduContent?> {
        val contentLiveData = MutableLiveData<EduContent?>()
        viewModelScope.launch {
            contentLiveData.postValue(repository.getContentById(contentID))
        }
        return contentLiveData
    }

    fun openUrl(context: Context, resourceUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl))
        context.startActivity(intent)
    }

}
