package com.example.findwork.ui.vacancies

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.findwork.MainActivity
import org.json.JSONObject

class VacanciesViewModel : ViewModel() {
    val response: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val filters: MutableLiveData<MutableMap<String , ArrayList<String>>> by lazy {
        MutableLiveData<MutableMap<String , ArrayList<String>>>()
    }
    val filtersResponse: MutableLiveData<JSONObject> by lazy {
        MutableLiveData<JSONObject>()
    }
    val positionRecyclerView: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val singleVacancyResponse: MutableLiveData<JSONObject> by lazy {
        MutableLiveData()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    val loadingVacancy: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    fun getVacancy(url:String){
        val queue = Volley.newRequestQueue(MainActivity.getContext())
        val stringRequest = object : JsonObjectRequest(
            Method.GET , url , null ,
            {
               singleVacancyResponse.value = it
                loading.value=false
            } ,
            {
                Toast.makeText(MainActivity.getContext() ,
                    "Подключение в серверу отсутсвует" ,
                    Toast.LENGTH_LONG).show()
                loading.value=false
            }) {
            override fun getHeaders(): Map<String , String> {
                val headers: MutableMap<String , String> = HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                return headers
            }
        }
        queue.add(stringRequest)
    }
}