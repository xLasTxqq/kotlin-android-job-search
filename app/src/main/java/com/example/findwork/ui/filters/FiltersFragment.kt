package com.example.findwork.ui.filters

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.databinding.FragmentHomeBinding
import com.example.findwork.ui.data.Searching
import com.example.findwork.ui.data.VolleyRequest
import com.example.findwork.ui.vacancies.VacanciesViewModel
import org.json.JSONObject


class FiltersFragment : Fragment() , VolleyRequest.Listener {

    private val dataModel: VacanciesViewModel by activityViewModels()

    private var request: MutableMap<String , ArrayList<String>> = HashMap()
    private lateinit var response: JSONObject
    private lateinit var binding: FragmentHomeBinding
    private val url = "https://thisistesttoo.000webhostapp.com/api/filters"

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater , container , false)

        binding.swipeRefresh.setOnRefreshListener {
            VolleyRequest(this).sendRequest(url , Request.Method.GET , null , false)
        }

        if (dataModel.filters.value != null) {
            request = dataModel.filters.value!!
            if (!(request["salary"].isNullOrEmpty()))
                binding.editTextSalary.setText(request["salary"]!![0])
            if (!(request["only_with_salary"].isNullOrEmpty()))
                binding.checkBoxSalary.isChecked = request["only_with_salary"]!![0].toBoolean()
        }

        if (dataModel.filtersResponse.value == null) {
            VolleyRequest(this).sendRequest(url , Request.Method.GET , null , false)
        } else {
            response = dataModel.filtersResponse.value!!
            for (i in 1..6)
                createAdapter(i , response)
        }

        binding.editTextSalary.addTextChangedListener {
            val salary =
                if (it.toString().isEmpty()) it.toString() else it.toString().toInt().toString()
            if (!isEmpty(salary) && salary.toInt() >= 1) {
                request["salary"] = arrayListOf(salary)
            } else request.remove("salary")

            dataModel.filters.value = request
            val body = if (request.isNullOrEmpty()) null
            else JSONObject(request as Map<* , *>)
            VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancy" , Request.Method.POST , body , false)
        }

        binding.checkBoxSalary.setOnCheckedChangeListener { _ , b ->
            request["only_with_salary"] = arrayListOf(b.toString())
            dataModel.filters.value = request
            val body = if (request.isNullOrEmpty()) null
            else JSONObject(request as Map<* , *>)
            VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancy" , Request.Method.POST , body , false)
        }

        return binding.root
    }

    private fun createAdapter(i: Int , res: JSONObject) {
        var name = ""
        val list: ArrayList<String> = arrayListOf()
        lateinit var progressBar: ProgressBar
        lateinit var listView: ListView
        var textReset: TextView? = null
        var searchView: android.widget.SearchView? = null
        val checked: ArrayList<Int> = arrayListOf()
        when (i) {
            1 -> {
                name = "experience"
                progressBar = binding.progressBarExperience
                listView = binding.listExperience
            }
            2 -> {
                name = "schedule"
                progressBar = binding.progressBarSchedule
                listView = binding.listSchedule
            }
            3 -> {
                name = "area"
                progressBar = binding.progressBarArea
                listView = binding.listArea
                textReset = binding.textResetArea
                searchView = binding.searchArea
            }
            4 -> {
                name = "specializations"
                progressBar = binding.progressBarSpecializations
                listView = binding.listSpecializations
                textReset = binding.textResetSpecializations
                searchView = binding.searchSpecializations
            }
            5 -> {
                name = "date_from"
                progressBar = binding.progressBarDate
                listView = binding.listDate
            }
            6 -> {
                name = "currency"
                progressBar = binding.progressBarSalary
                listView = binding.listSalary
            }
        }
        //Кнопка сбросить
        textReset?.let {
            it.setOnClickListener {
                for (j in 0 until listView.count) {
                    listView.setItemChecked(j , false)
                }
                request[name] = arrayListOf()
                val body = if (request.isNullOrEmpty()) null
                else JSONObject(request as Map<* , *>)
                VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancy" , Request.Method.POST , body , false)
            }
        }
        //Поиск
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener ,
            android.widget.SearchView.OnQueryTextListener {

            fun send(text: String) {
                val body = JSONObject()
                body.put("text" , text)
                body.put("search" , name)
                createAdapter(i , Searching().search(text , dataModel.filtersResponse.value?.getJSONArray(name) , name))
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (isEmpty(newText)) {
                    createAdapter(i , response)
                } else {
                    send(newText)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (isEmpty(query)) {
                    createAdapter(i , response)
                } else {
                    send(query)
                }
                return true
            }

        })
        //Добавление списка
        for (j in 0 until res.getJSONArray(name).length()) {
            list.add(res.getJSONArray(name).getJSONObject(j).getString("name"))
            if (res.getJSONArray(name).getJSONObject(j)
                    .has("id") && request[name]?.contains(res.getJSONArray(name).getJSONObject(j)
                    .getString("id")) == true
            )
                checked.add(j)
            if (res.getJSONArray(name).getJSONObject(j)
                    .has("code") && request[name]?.contains(res.getJSONArray(name).getJSONObject(j)
                    .getString("code")) == true
            )
                checked.add(j)
        }
        listView.adapter = ArrayAdapter(MainActivity.getContext() ,
            android.R.layout.simple_list_item_multiple_choice ,
            list)
        //Отображение выбраных элементов
        if (listView.choiceMode == ListView.CHOICE_MODE_SINGLE)
            listView.setItemChecked(0 , true)
        for (j in checked) {
            listView.setItemChecked(j , true)
        }
        //Завершение загрузки
        progressBar.visibility = View.GONE
        listView.isNestedScrollingEnabled = true
        binding.swipeRefresh.isRefreshing = false
        //Нажатия на элементы списков
        listView.onItemClickListener = AdapterView.OnItemClickListener { _ , _ , j , _ ->
            val id: String = if (res.getJSONArray(name).getJSONObject(j).has("id"))
                res.getJSONArray(name).getJSONObject(j).getString("id")
            else res.getJSONArray(name).getJSONObject(j).getString("code")
            if (listView.choiceMode == ListView.CHOICE_MODE_SINGLE)
                request[name] = arrayListOf(id)
            else
                if (listView.isItemChecked(j)) {
                    if (request[name].isNullOrEmpty()) request[name] = arrayListOf(id)
                    else request[name]?.add(id)
                } else request[name]?.remove(id)

            dataModel.filters.value = request

            val body = if (request.isNullOrEmpty()) null
            else JSONObject(request as Map<* , *>)
            VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancy" , Request.Method.POST , body , false)
        }
    }

    override fun onResponse(response: JSONObject) {
        if (response.has("experience")) {
            this.response = response
            dataModel.filtersResponse.value = response
            for (i in 1..6)
                createAdapter(i , response)
        } else {
                dataModel.positionRecyclerView.value = null
                dataModel.response.value = response
                println(dataModel.response.value)
        }
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onError(response: VolleyError) {
        Toast.makeText(MainActivity.getContext() ,
            "Подключение в серверу отсутсвует" ,
            Toast.LENGTH_LONG).show()
        binding.swipeRefresh.isRefreshing = false
    }
}