package com.example.findwork.ui.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class SummaryViewModel : ViewModel() {
    val summary: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val summaryRequest: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}