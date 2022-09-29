package com.example.findwork.ui.employer_vacancies

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class EmployerVacanciesViewModel:ViewModel() {
    val vacancies: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val summary: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
}