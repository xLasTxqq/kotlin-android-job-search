package com.example.findwork.ui.vacancies

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.Volley
import com.example.findwork.MainActivity
import com.example.findwork.UsageDB
import com.example.findwork.ui.data.VolleyMultipartRequest


class NewVacancyViewModel : ViewModel() {

    val request: MutableLiveData<MutableMap<String , String>> by lazy {
        MutableLiveData<MutableMap<String , String>>()
    }
    val loading: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }
    val uriImage: MutableLiveData<Uri> by lazy { MutableLiveData() }

    private fun createImageData(uri: Uri):ByteArray? {
            val inputStream = MainActivity.getContext().contentResolver.openInputStream(uri)
            inputStream?.buffered()?.use {
                return it.readBytes()
            }
            return null
    }

    fun createVacancy(method: Int , url: String , body: Map<String,String>){
            val queue = Volley.newRequestQueue(MainActivity.getContext())
            val stringRequest = object : VolleyMultipartRequest(
                method , url ,
                {
                    println(it)
                    when{
                        it.has("id")->{
                            Toast.makeText(MainActivity.getContext() ,
                                "Вакансия успешно создана" ,
                                Toast.LENGTH_LONG).show()
                        }
                        it.has("errors") -> {
                            Toast.makeText(MainActivity.getContext() ,
                                it.getString("errors"),
                                Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(MainActivity.getContext() ,
                                "Непредвиденная ошибка, попробуйте еще раз" ,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    loading.value=false
                } ,
                {
                    var error = "Ошибка подключения к серверу, попробуйте еще раз позже"
                    if (it.networkResponse?.statusCode == 401) {
                        UsageDB(MainActivity.getContext()).deleteDB(null , null)
                        MainActivity.checkUser(true)
                        error="Вход в аккаунт больше не действителен"
                    }
                    Toast.makeText(MainActivity.getContext() ,
                        error , Toast.LENGTH_LONG).show()
                    loading.value = false
                }) {

                override fun getByteData(): Map<String , DataPart> {
                    val bodyData = HashMap<String, DataPart>()
                    if(uriImage.value!=null) {
                        val imageData = createImageData(uriImage.value!!)
                        val imageType = MainActivity.getContext().contentResolver.getType(uriImage.value!!)
                        if (imageData != null)
                            bodyData["image"] = DataPart("image" , imageData , imageType!!)
                    }
                    return bodyData
                }

                override fun getParams(): Map<String , String> {
                    println(body)
                    return body
                }

                override fun getHeaders(): Map<String , String> {
                    val headers: MutableMap<String , String> = HashMap()
                    headers["Accept"] = "application/json"
                    headers["Authorization"] = "Bearer "+ UsageDB(MainActivity.getContext()).readDB(null , null , null , "id" , 0)[0][1]
                    println(headers["Authorization"])
                    return headers
                }
            }
        println("Отправил")
        stringRequest.retryPolicy = DefaultRetryPolicy(0 ,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES ,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
    }
}

