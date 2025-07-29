package com.dawnbellsuha.uha.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _selectedClass = MutableLiveData<String>()
    val selectedClass: LiveData<String> = _selectedClass

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
    }
}