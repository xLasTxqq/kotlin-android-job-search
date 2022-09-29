package com.example.findwork.ui.data

import org.json.JSONArray
import org.json.JSONObject

class Searching {
    fun search(text: String , body: JSONArray? , search: String): JSONObject {
//        println(text)
//        println(body)
//        println(search)
        val searching = if (search == "area") "areas" else search
        val result = JSONObject()
        val resultArray = JSONArray()
        if(body != null) {
            var key = 0
            for (j in 0 until body.length()) {
                if (body.getJSONObject(j).has(searching)) {
                    for (i in 0 until body.getJSONObject(j).getJSONArray(searching).length()) {
                        if (body.getJSONObject(j).getJSONArray(searching).getJSONObject(i)
                                .has(searching)
                        ) {
                            for (k in 0 until body.getJSONObject(j).getJSONArray(searching)
                                .getJSONObject(i).getJSONArray(searching).length()) {
                                if (body.getJSONObject(j).getJSONArray(searching).getJSONObject(i)
                                        .getJSONArray(searching).getJSONObject(k).getString("name")
                                        .indexOf(text) != -1
                                ) {
                                    resultArray.put(key ,
                                        JSONObject(hashMapOf("id" to body.getJSONObject(j)
                                            .getJSONArray(searching)
                                            .getJSONObject(i).getJSONArray(searching)
                                            .getJSONObject(k)
                                            .getString("id") ,
                                            "name" to body.getJSONObject(j).getJSONArray(searching)
                                                .getJSONObject(i).getJSONArray(searching)
                                                .getJSONObject(k)
                                                .getString("name")) as Map<* , *>))
                                    key++
                                }
                            }
                        }
                        if (body.getJSONObject(j).getJSONArray(searching).getJSONObject(i)
                                .getString("name").indexOf(text) != -1
                        ) {
                            resultArray.put(key ,
                                JSONObject(hashMapOf("id" to body.getJSONObject(j)
                                    .getJSONArray(searching)
                                    .getJSONObject(i).getString("id") ,
                                    "name" to body.getJSONObject(j).getJSONArray(searching)
                                        .getJSONObject(i).getString("name")) as Map<* , *>))
                            key++
                        }
                    }
                }
                if (body.getJSONObject(j).getString("name").indexOf(text) != -1) {
                    resultArray.put(key ,
                        JSONObject(hashMapOf("name" to body.getJSONObject(j).getString("name") ,
                            "id" to body.getJSONObject(j).getString("id")) as Map<* , *>))
                    key++
                }
            }
        }
//        println(result.put(search,resultArray))
        return result.put(search,resultArray)

    }
}