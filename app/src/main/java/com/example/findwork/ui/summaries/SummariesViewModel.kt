package com.example.findwork.ui.summaries

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class SummariesViewModel : ViewModel() {
    val summaries: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}