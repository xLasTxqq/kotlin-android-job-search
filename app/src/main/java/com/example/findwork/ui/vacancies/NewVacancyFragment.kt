package com.example.findwork.ui.vacancies

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.FragmentNewVacancyBinding
import com.example.findwork.ui.data.Searching
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONObject


class NewVacancyFragment : Fragment() , VolleyRequest.Listener {
    private val dataModel: VacanciesViewModel by activityViewModels()
    private val dataModelNewVacancy: NewVacancyViewModel by activityViewModels()
    private lateinit var binding: FragmentNewVacancyBinding
    private val url = "https://thisistesttoo.000webhostapp.com/api/create/vacancy"
    private var request: MutableMap<String , String> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentNewVacancyBinding.inflate(inflater , container , false)
        setHasOptionsMenu(true)

        if (!dataModelNewVacancy.request.value.isNullOrEmpty()) {
            request = dataModelNewVacancy.request.value!!
        }
        for (i in 1..7) {
            updateInputs(i)
        }

        dataModelNewVacancy.uriImage.observe(viewLifecycleOwner) {
            binding.imagePicture.setImageURI(it)
            binding.imagePicture.visibility = View.VISIBLE
        }

        dataModel.filtersResponse.observe(viewLifecycleOwner) {
            for (i in 1..5)
                createAdapter(i , it)
        }
        binding.swipeRefresh.setOnRefreshListener {
//            sendRequest(Request.Method.GET , "https://thisistesttoo.000webhostapp.com/api/filters")
            VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/filters" ,
                Request.Method.GET ,
                null ,
                false)
        }
        if (dataModel.filtersResponse.value == null) {
            VolleyRequest(this).sendRequest("https://thisistesttoo.000webhostapp.com/api/filters" ,
                Request.Method.GET ,
                null ,
                false)
//            sendRequest(Request.Method.GET , "https://thisistesttoo.000webhostapp.com/api/filters")
        }
        binding.file.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent , 0)
        }
        binding.submit.setOnClickListener {
//            dataModelNewVacancy.createVacancy(Request.Method.POST , url , body = if (request.isNullOrEmpty()) null
//            else JSONObject(request as Map<* , *>))
            updateBodyRequest()
            dataModelNewVacancy.createVacancy(Request.Method.POST ,
                url ,
                dataModelNewVacancy.request.value!!)
//            dataModelNewVacancy.createVacancy(Request.Method.POST ,
//                url ,
//                hashMapOf("area" to "113","schedule" to "fullDay",
//                    "specializations" to "1", "name" to "ааааа", "description" to "fghfghfghfg",
//                    "currency" to "AZN", "experience" to "noExperience")
//                    )
//            VolleyRequest(this).sendRequest(url,Request.Method.POST, JSONObject("{\"area\":\"113\", \"schedule\":\"fullDay\", \"specializations\":\"1\", \"name\":\"hgfh\", \"description\":\"fghfghfghfg\", \"currency\":\"AZN\", \"experience\":\"noExperience\"}"), true)
            dataModelNewVacancy.loading.value = true
        }
        dataModelNewVacancy.loading.observe(viewLifecycleOwner) {
            if (it == false) {
                if (binding.inputName.text.isNotEmpty()
                    || binding.inputDescription.text.isNotEmpty()
                    || binding.editContactName.text.isNotEmpty()
                    || binding.editContactEmail.text.isNotEmpty()
                    || binding.editContactPhone.text.isNotEmpty()
                    || binding.editTextSalaryTo.text.isNotEmpty()
                    || binding.editTextSalaryFrom.text.isNotEmpty()
                )
                    validate()
                binding.loading.visibility = View.INVISIBLE
                binding.swipeRefresh.isRefreshing = false
            } else {
                validate()
                binding.loading.visibility = View.VISIBLE
            }
        }

        binding.editContactPhone.setOnEditorActionListener { _ , _ , _ ->
            if (binding.submit.isEnabled) binding.submit.callOnClick()
            false
        }

        return binding.root
    }

    private fun updateInputs(i: Int) {
        lateinit var name: String
        lateinit var input: EditText
        when (i) {
            1 -> {
                name = "name"
                input = binding.inputName
            }
            2 -> {
                name = "description"
                input = binding.inputDescription
            }
            3 -> {
                name = "contacts_name"
                input = binding.editContactName
            }
            4 -> {
                name = "contacts_email"
                input = binding.editContactEmail
            }
            5 -> {
                name = "contacts_phones"
                input = binding.editContactPhone
            }
            6 -> {
                name = "salary_to"
                input = binding.editTextSalaryTo
            }
            7 -> {
                name = "salary_from"
                input = binding.editTextSalaryFrom
            }
        }
        input.setText(request[name])
        input.addTextChangedListener {
            validate()
        }
    }

    private fun createAdapter(i: Int , res: JSONObject) {
        var name = ""
        val list: ArrayList<String> = arrayListOf()
        lateinit var progressBar: ProgressBar
        lateinit var listView: ListView
        var searchView: android.widget.SearchView? = null
        var checked: Int? = null
        when (i) {
            1 -> {
                name = "area"
                progressBar = binding.progressBarArea
                listView = binding.listArea
                searchView = binding.searchArea
            }
            2 -> {
                name = "currency"
                progressBar = binding.progressBarSalary
                listView = binding.listSalary
            }
            3 -> {
                name = "experience"
                progressBar = binding.progressBarExperience
                listView = binding.listExperience
            }
            4 -> {
                name = "schedule"
                progressBar = binding.progressBarSchedule
                listView = binding.listSchedule
            }
            5 -> {
                name = "specializations"
                progressBar = binding.progressBarSpecializations
                listView = binding.listSpecializations
                searchView = binding.searchSpecializations
            }
        }
        //Поиск
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener ,
            android.widget.SearchView.OnQueryTextListener {

            fun send(text: String) {
                val body = JSONObject()
                body.put("text" , text)
                body.put("search" , name)
                createAdapter(i ,
                    Searching().search(text ,
                        dataModel.filtersResponse.value?.getJSONArray(name) ,
                        name))
//                sendRequest(Request.Method.POST ,
//                    "https://thisistesttoo.000webhostapp.com/api/search" ,
//                    i ,
//                    body)
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    if (dataModel.filtersResponse.value != null)
                        createAdapter(i , dataModel.filtersResponse.value!!)
                } else {
                    send(newText)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (TextUtils.isEmpty(query)) {
                    if (dataModel.filtersResponse.value != null)
                        createAdapter(i , dataModel.filtersResponse.value!!)
                } else {
                    send(query)
                }
                return true
            }

        })
        //Добавление списка
        for (j in 0 until res.getJSONArray(name).length()) {
            list.add(res.getJSONArray(name).getJSONObject(j).getString("name"))
            if (res.getJSONArray(name).getJSONObject(j).has("id")
                && request[name] == res.getJSONArray(name).getJSONObject(j).getString("id")
            )
                checked = j
            if (res.getJSONArray(name).getJSONObject(j).has("code")
                && request[name] == res.getJSONArray(name).getJSONObject(j).getString("code")
            )
                checked = j
        }

        listView.adapter = ArrayAdapter(MainActivity.getContext() ,
            android.R.layout.simple_list_item_multiple_choice ,
            list)
        //Нажатия на элементы списков
        listView.onItemClickListener = AdapterView.OnItemClickListener { _ , _ , j , _ ->
            val id: String = if (res.getJSONArray(name).getJSONObject(j).has("id"))
                res.getJSONArray(name).getJSONObject(j).getString("id")
            else res.getJSONArray(name).getJSONObject(j).getString("code")
            request[name] = id
            dataModelNewVacancy.request.value = request
        }
        //Выбранный элемент
        if (checked != null)
            listView.setItemChecked(checked , true)
        else if (!request.contains(name)) {
            listView.setItemChecked(0 , true)
            request[name] = if (res.getJSONArray(name).getJSONObject(0).has("id"))
                res.getJSONArray(name).getJSONObject(0).getString("id")
            else res.getJSONArray(name).getJSONObject(0).getString("code")
            println(request)
            dataModelNewVacancy.request.value = request
        }
        //Завершение загрузки
        progressBar.visibility = View.GONE
        listView.isNestedScrollingEnabled = true
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if(data.clipData?.getItemAt(0)?.uri==null)
                    Toast.makeText(this.context , "Не удалось загрузить картинку, попробуйте другое приложение с фотографиями" , Toast.LENGTH_LONG)
                    .show()
                else {
                    binding.imagePicture.setImageURI(data.clipData?.getItemAt(0)?.uri)
                    binding.imagePicture.visibility = View.VISIBLE
                    dataModelNewVacancy.uriImage.value = data.clipData?.getItemAt(0)?.uri
                }
//                dataModelNewVacancy.image.value = (binding.imagePicture.drawable as BitmapDrawable).bitmap
//                println((binding.imagePicture.drawable as BitmapDrawable).bitmap)
//                println(data.data?.path)
            } else {
                Toast.makeText(this.context , "Вы не выбрали картинку!" , Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            super.onActivityResult(requestCode , resultCode , data)
            Toast.makeText(this.context , "Вы не выбрали картинку!" , Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu , inflater: MenuInflater) {
        inflater.inflate(R.menu.log_out_of_account_menu , menu)
        super.onCreateOptionsMenu(menu , inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                UsageDB(MainActivity.getContext()).deleteDB(null , null)
                MainActivity.checkUser(true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun sendRequest(method: Int , url: String , col: Int? = null , body: Any? = null) {
//        val queue = Volley.newRequestQueue(MainActivity.getContext())
//        val stringRequest = object : JsonObjectRequest(
//            method , url , null ,
//            {
//                if (method == Method.GET) {
//                    dataModel.filtersResponse.value = it
////                    for (i in 1..6)
////                        createAdapter(i , it)
//                } else {
////                    dataModelNewVacancy.response.value = it
//                    if (col != null) {
//                        createAdapter(col , it)
//                    }
//                    //                    else {
////                        dataModel.positionRecyclerView.value = null
////                        dataModel.response.value = it
////                        println(dataModel.response.value)
////                    }
//                }
//            } ,
//            {
//                Toast.makeText(MainActivity.getContext() ,
//                    "Подключение в серверу отсутсвует" ,
//                    Toast.LENGTH_LONG).show()
//                binding.swipeRefresh.isRefreshing = false
//            }) {
//            override fun getBody(): ByteArray {
//                return body.toString().toByteArray()
//            }
//
//            override fun getHeaders(): Map<String , String> {
//                val headers: MutableMap<String , String> = HashMap()
//                headers["Content-Type"] = "application/json"
//                headers["Accept"] = "application/json"
//                return headers
//            }
//        }
//        queue.add(stringRequest)
//    }

    private fun updateBodyRequest() {
        if (binding.inputName.text.isNotEmpty()) request["name"] = binding.inputName.text.toString()
        else request.remove("name")
        if (binding.inputDescription.text.isNotEmpty()) request["description"] =
            binding.inputDescription.text.toString()
        else request.remove("description")
        if (binding.editContactName.text.isNotEmpty()) request["contacts_name"] =
            binding.editContactName.text.toString()
        else request.remove("contacts_name")
        if (binding.editContactEmail.text.isNotEmpty()) request["contacts_email"] =
            binding.editContactEmail.text.toString()
        else request.remove("contacts_email")
        if (binding.editContactPhone.text.isNotEmpty()) request["contacts_phones"] =
            binding.editContactPhone.text.toString()
        else request.remove("contacts_phones")
        if (binding.editTextSalaryTo.text.isNotEmpty()) request["salary_to"] =
            binding.editTextSalaryTo.text.toString()
        else request.remove("salary_to")
        if (binding.editTextSalaryFrom.text.isNotEmpty()) request["salary_from"] =
            binding.editTextSalaryFrom.text.toString()
        else request.remove("salary_from")
        dataModelNewVacancy.request.value = request
    }

    override fun onPause() {
        super.onPause()
        updateBodyRequest()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateBodyRequest()
    }

    private fun validate() {
        binding.apply {
            if (inputName.text.isBlank()) {
                inputName.error = "Название вакансии не может быть пустым"
                binding.submit.isEnabled = false
            } else if (editTextSalaryTo.text.isNotBlank() && editTextSalaryFrom.text.isNotBlank()
                && editTextSalaryTo.text.toString().toDouble() < editTextSalaryFrom.text.toString()
                    .toDouble()
            ) {
                editTextSalaryTo.error = "Максимальная ЗП не может быть меньше минимальной"
                binding.submit.isEnabled = false
            } else if (inputDescription.text.isBlank()) {
                inputDescription.error = "Опишите задачи работника"
                binding.submit.isEnabled = false
            } else if (editContactEmail.text.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(
                    editContactEmail.text).matches()
            ) {
                editContactEmail.error = "Не валидная почта"
                binding.submit.isEnabled = false
            } else if (editContactPhone.text.isNotBlank() && !Patterns.PHONE.matcher(
                    editContactPhone.text).matches()
            ) {
                editContactPhone.error = "Не валидный телефон"
                binding.submit.isEnabled = false
            } else {
                submit.isEnabled = dataModelNewVacancy.loading.value == false
                inputName.error = null
                editTextSalaryTo.error = null
                editTextSalaryFrom.error = null
                inputDescription.error = null
                editContactName.error = null
                editContactEmail.error = null
                editContactPhone.error = null
            }
        }
    }

    override fun onResponse(response: JSONObject) {
        dataModel.filtersResponse.value = response
    }

    override fun onError(response: VolleyError) {
        Toast.makeText(MainActivity.getContext() ,
            "Подключение в серверу отсутсвует" ,
            Toast.LENGTH_LONG).show()
        binding.swipeRefresh.isRefreshing = false
    }
}