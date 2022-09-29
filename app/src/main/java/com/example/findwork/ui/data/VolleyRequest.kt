package com.example.findwork.ui.data

import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.findwork.MainActivity
import com.example.findwork.UsageDB
import org.json.JSONObject

class VolleyRequest(val listener: Listener) {
    fun sendRequest(url:String, method:Int, body:JSONObject?, auth:Boolean) {
        val queue = Volley.newRequestQueue(MainActivity.getContext())
        val stringRequest = object : JsonObjectRequest(
            method , url, null ,
            {
                listener.onResponse(it)
            } ,
            {
                listener.onError(it)
            }
        ) {
            override fun getBody(): ByteArray {
                return body.toString().toByteArray()
            }

            override fun getHeaders(): Map<String , String> {
                val headers: MutableMap<String , String> =
                    HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"

                if(auth)
                    headers["Authorization"] = "Bearer " + UsageDB(MainActivity.getContext()).readDB(
                        null ,
                        null ,
                        null ,
                        "id" ,
                        0)[0][1]
                return headers
            }
        }
        queue.add(stringRequest)
    }
    interface Listener{
        fun onResponse(response:JSONObject)
        fun onError(response:VolleyError)
    }
}