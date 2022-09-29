package com.example.findwork.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class DataAuthViewModel: ViewModel() {
    val bodyRequest: MutableLiveData<JSONObject> by lazy { MutableLiveData() }
    val loading: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }
    //ViewModelProvider(this).get(DataAuthViewModel::class.java)
}