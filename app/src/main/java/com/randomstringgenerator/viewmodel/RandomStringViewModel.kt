package com.randomstringgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.randomstringgenerator.data.RandomStringMetadata
import com.randomstringgenerator.data.RandomStringRepository
import kotlinx.coroutines.launch

class RandomStringViewModel(private val repository: RandomStringRepository) : ViewModel() {

    private val _stringList = MutableLiveData<List<RandomStringMetadata>>(emptyList())
    val stringList: LiveData<List<RandomStringMetadata>> = _stringList
    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> = _errorMsg

    fun generateString(maxLength: Int) {
        viewModelScope.launch {
            val result = repository.fetchRandomString(maxLength)
            result?.let {
                val updated = stringList.value.orEmpty() + it
                _stringList.postValue(updated)
                _errorMsg.postValue(null)
            } ?: _errorMsg.postValue("Failed to query provider")
        }
    }

    fun deleteAll() {
        _stringList.postValue(emptyList())
    }

    fun deleteAt(index: Int) {
        val updated = stringList.value.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
        }
        _stringList.postValue(updated)
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
