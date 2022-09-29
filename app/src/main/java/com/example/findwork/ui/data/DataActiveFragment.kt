package com.example.findwork.ui.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataActiveFragment : ViewModel() {
    val activeFragment:MutableLiveData<Int> by lazy { MutableLiveData() }
}