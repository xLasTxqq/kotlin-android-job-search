package com.example.findwork.ui.employer_vacancies

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.FragmentEmpoyerVacanciesBinding
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject


class EmployerVacanciesFragment : Fragment(), ExpandableList.Listener {

    private val dataModel: EmployerVacanciesViewModel by activityViewModels()
    private val dataModelUpdateVacancy: UpdateVacancyViewModel by activityViewModels()
    private lateinit var binding: FragmentEmpoyerVacanciesBinding
    private lateinit var adapter: ExpandableList
    private lateinit var navController: NavController

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentEmpoyerVacanciesBinding.inflate(inflater , container , false)
        setHasOptionsMenu(true)

        navController = findNavController()
        dataModel.vacancies.observe(viewLifecycleOwner){
            if(it.has("items")) {
                adapter =
                    ExpandableList(if (dataModel.vacancies.value != null) dataModel.vacancies.value!! else JSONObject(),this)
                binding.listExp.setAdapter(adapter)
                binding.listExp.setOnChildClickListener { _ , _ , i , i2 , _ ->
                    dataModel.summary.value = dataModel.vacancies.value!!.getJSONArray("items").getJSONObject(i).getJSONArray("summary").getJSONObject(i2)
                    EmployerSummary().show((activity as MainActivity).supportFragmentManager, "MyCustomFragment")
                    false
                }
                if(it.getJSONArray("items").length()>0) {
                    var summaries = 0
                    for (j in 0 until it.getJSONArray("items").length())
                        summaries += it.getJSONArray("items").getJSONObject(j)
                            .getJSONArray("summary").length()

                    binding.textView.text = "Найдено вакансий: ${
                        it.getJSONArray("items").length()
                    } и отправленных резюме: $summaries"
                    binding.textView.textSize = 14F
                    binding.textView.setTypeface(null, Typeface.NORMAL)
                }
                else {
                    binding.textView.text = "Вакансий не найдено"
                    binding.textView.textSize = 20F
                    binding.textView.setTypeface(null, Typeface.BOLD)
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            dataModel.loading.value = true
            sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancies")
        }

        dataModel.loading.observe(viewLifecycleOwner) {
            if (it && !binding.swipeRefresh.isRefreshing) {
                binding.loading.visibility = View.VISIBLE
            } else {
                binding.loading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }

        if (dataModel.vacancies.value == null) {
            dataModel.loading.value = true
            sendRequest("https://thisistesttoo.000webhostapp.com/api/vacancies")
        }
        return binding.root
    }

    private fun sendRequest(url: String) {
        val queue = Volley.newRequestQueue(MainActivity.getContext())
        val stringRequest = object : JsonObjectRequest(
            Method.GET , url , null ,
            {
                println(it)
                when{
                    it.has("items") -> dataModel.vacancies.value = it
                    it.has("errors") -> {
                        Toast.makeText(this.context, it.getString("errors"), Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(this.context, "Не предвиденная ошибка", Toast.LENGTH_LONG).show()
                }
                dataModel.loading.value=false
            } ,
            {
                Toast.makeText(this.context ,
                    "Ошибка подключения к серверу, попробуйте еще раз позже" ,
                    Toast.LENGTH_LONG).show()
                dataModel.loading.value=false
            }
        ) {
            override fun getHeaders(): Map<String , String> {
                val headers: MutableMap<String , String> =
                    HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
                headers["Authorization"] = "Bearer " + UsageDB(MainActivity.getContext()).readDB(
                    null ,
                    null ,
                    null ,
                    "id" ,
                    0)[0][1]
                println(headers["Authorization"])
                return headers
            }
        }
        queue.add(stringRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu , inflater: MenuInflater) {
        inflater.inflate(R.menu.logout_with_delete , menu)
        super.onCreateOptionsMenu(menu , inflater)
    }

    private fun deleteVacancy(id:String? = null){
        val dialog = AlertDialog.Builder(this.context)
        val text = if (id.isNullOrBlank()) "Вы уверены, что хотите удалить все вакансии?" else "Вы уверены, что хотите удалить вакансию?"
        val textRemove = if(id.isNullOrBlank()) "Все вакансии сейчас удалятся" else "Вакансия сейчас удалится"
        val url = if(id.isNullOrBlank())"https://thisistesttoo.000webhostapp.com/api/delete/vacancy"
        else "https://thisistesttoo.000webhostapp.com/api/delete/vacancy/$id"
        dialog.setCancelable(true)
            .setTitle(text)
            .setPositiveButton("Да, я хочу удалить!") { _ , _ ->
                Snackbar.make(binding.root,textRemove, Snackbar.LENGTH_LONG).show()
                sendRequest(url)
                dataModel.loading.value = true
            }
            .setNegativeButton("Отмена") { dialogAlert , _ ->
                dialogAlert.cancel()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                UsageDB(MainActivity.getContext()).deleteDB(null , null)
                MainActivity.checkUser(true)
                return true
            }
            R.id.deleteAll -> {
                deleteVacancy()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(id: String) {
        deleteVacancy(id)
    }

    override fun onChange(id: String , status: Boolean) {
        sendPostRequest(id,status)
    }

    override fun onUpdate(vacancy: JSONObject) {
        val request:MutableMap<String,String> = HashMap()
        request["id"] = vacancy.getString("id")
        if(!vacancy.isNull("name"))request["name"] = vacancy.getString("name")
        if(!vacancy.isNull("description"))request["description"] = vacancy.getString("description")
        if(!vacancy.isNull("contacts")&&!vacancy.getJSONObject("contacts").isNull("name"))
            request["contacts_name"] = vacancy.getJSONObject("contacts").getString("name")
        if(!vacancy.isNull("contacts")&&!vacancy.getJSONObject("contacts").isNull("email"))
            request["contacts_email"] = vacancy.getJSONObject("contacts").getString("email")
        if(!vacancy.isNull("contacts")&&!vacancy.getJSONObject("contacts").isNull("phone"))
            request["contacts_phones"] = vacancy.getJSONObject("contacts").getString("phone")
        if(!vacancy.isNull("salary")&&!vacancy.getJSONObject("salary").isNull("to"))
            request["salary_to"] = vacancy.getJSONObject("salary").getString("to")
        if(!vacancy.isNull("salary")&&!vacancy.getJSONObject("salary").isNull("from"))
            request["salary_from"] = vacancy.getJSONObject("salary").getString("from")
        if(!vacancy.isNull("area")&&!vacancy.getJSONObject("area").isNull("id")) //TODO изменить name на id
            request["area"] = vacancy.getJSONObject("area").getString("id")
        if(!vacancy.isNull("salary")&&!vacancy.getJSONObject("salary").isNull("currency"))
            request["currency"] = vacancy.getJSONObject("salary").getString("currency")
        if(!vacancy.isNull("experience")&&!vacancy.getJSONObject("experience").isNull("id")) //TODO изменить name на id
            request["experience"] = vacancy.getJSONObject("experience").getString("id")
        if(!vacancy.isNull("schedule")&&!vacancy.getJSONObject("schedule").isNull("id")) //TODO изменить name на id
            request["schedule"] = vacancy.getJSONObject("schedule").getString("id")
        if(!vacancy.isNull("specialization")&&!vacancy.getJSONObject("specialization").isNull("id")) //TODO добавить специализацию
            request["specialization"] = vacancy.getJSONObject("specialization").getString("id")

        dataModelUpdateVacancy.request.value = request
        navController.navigate(R.id.navigation_update_vacancy)
//        TODO("Сделать изменения вакансии")
    }

    private fun sendPostRequest(id: String , status: Boolean) {
        val queue = Volley.newRequestQueue(MainActivity.getContext())
        val stringRequest = object : JsonObjectRequest(
            Method.POST , "https://thisistesttoo.000webhostapp.com/api/summaries/$id" , null ,
            {
//                println(it)
                when{
                    it.has("items") -> dataModel.vacancies.value = it
                    it.has("errors") -> {
                        Toast.makeText(this.context, it.getString("errors"), Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(this.context, "Не предвиденная ошибка", Toast.LENGTH_LONG).show()
                }
                dataModel.loading.value=false
            } ,
            {
                var error = "Ошибка подключения к серверу, попробуйте еще раз позже"
                if (it.networkResponse?.statusCode == 401) {
                    UsageDB(MainActivity.getContext()).deleteDB(null , null)
                    MainActivity.checkUser(true)
                    error="Вход в аккаунт больше не действителен"
                }
                Toast.makeText(this.context ,
                    error , Toast.LENGTH_LONG).show()
                dataModel.loading.value = false
            }
        ) {
            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("status", status)
//                println(body)
                return body.toString().toByteArray()
            }
            override fun getHeaders(): Map<String , String> {
                val headers: MutableMap<String , String> =
                    HashMap()
                headers["Content-Type"] = "application/json"
                headers["Accept"] = "application/json"
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
}