package com.randomstringgenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.randomstringgenerator.data.RandomStringMetadata
import com.randomstringgenerator.data.RandomStringRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RandomStringViewModel(private val repository: RandomStringRepository) : ViewModel() {

    private val _stringList = MutableStateFlow<List<RandomStringMetadata>>(emptyList())
    val stringList: StateFlow<List<RandomStringMetadata>> = _stringList
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled

    fun generateString(maxLength: Int) {
        viewModelScope.launch {
            _isEnabled.emit(false)
            val result = repository.fetchRandomString(maxLength)
            result?.let {
                val updated = stringList.value + it
                _stringList.emit(updated)
                _errorMsg.emit(null)
                _isEnabled.emit(true)
            } ?: handleError()
        }
    }

    private suspend fun handleError() {
        _errorMsg.emit("Failed to query provider")
        _isEnabled.emit(true)
    }

    fun deleteAll() {
        viewModelScope.launch {
            _stringList.emit(emptyList())
        }
    }

    fun deleteAt(index: Int) {
        viewModelScope.launch {
            val updated = stringList.value.orEmpty().toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            _stringList.emit(updated)
        }
    }
}

class RandomStringViewModelFactory(private val repository: RandomStringRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RandomStringViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RandomStringViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
