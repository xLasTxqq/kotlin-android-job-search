package com.example.findwork.ui.summaries

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.FragmentSummariesBinding
import com.example.findwork.ui.data.VolleyRequest
import com.example.findwork.ui.vacancies.DialogVacancyFragment
import com.example.findwork.ui.vacancies.VacanciesViewModel
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class SummariesFragment : Fragment() , SummariesRecyclerView.Listener , VolleyRequest.Listener {
    lateinit var binding: FragmentSummariesBinding
    private val dataModel: SummariesViewModel by activityViewModels()
    private val dataModelVacancy: VacanciesViewModel by activityViewModels()
    private val adapter: SummariesRecyclerView = SummariesRecyclerView(this)
    private val url = "https://thisistesttoo.000webhostapp.com/api/summaries"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentSummariesBinding.inflate(inflater , container , false)
        setHasOptionsMenu(true)

        binding.recyclerView.layoutManager = LinearLayoutManager(MainActivity.getContext())
        binding.recyclerView.adapter = adapter

        dataModel.loading.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == true && !binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
        if (dataModel.summaries.value == null) {
            VolleyRequest(this).sendRequest(url , Request.Method.GET , null , true)
            dataModel.loading.value = true
        }
        dataModel.summaries.observe(viewLifecycleOwner) {
            adapter.removeSummary()
            if (it.has("items") && !it.isNull("items")) {
                val items = it.getJSONArray("items")
                binding.text.text = "Отправленных резюме найдено: " + items.length()
                for (j in 0 until items.length()) {
                    val statusSummary =
                        if (items.getJSONObject(j).has("status") && !items.getJSONObject(j)
                                .isNull("status")
                        )
                            items.getJSONObject(j).getString("status")
                        else "Не указано"
                    val idSummary = items.getJSONObject(j).getString("id")

                    val dateSummary =
                        if (items.getJSONObject(j).has("updated_at") && !items.getJSONObject(j)
                                .isNull("updated_at")
                        )
                            items.getJSONObject(j).getString("updated_at")
                        else "Не указано"
                    var nameVacancy = "Не указано"
                    var companyVacancy = "Не указано"
                    var idVacancy: String? = null
                    if (items.getJSONObject(j).has("vacancy") && !items.getJSONObject(j)
                            .isNull("vacancy")
                    ) {
                        if (items.getJSONObject(j).getJSONObject("vacancy")
                                .has("id") && !items.getJSONObject(j).getJSONObject("vacancy")
                                .isNull("id")
                        )
                            idVacancy =
                                items.getJSONObject(j).getJSONObject("vacancy").getString("id")
                        if (items.getJSONObject(j).getJSONObject("vacancy")
                                .has("name") && !items.getJSONObject(j).getJSONObject("vacancy")
                                .isNull("name")
                        )
                            nameVacancy = items.getJSONObject(j).getJSONObject("vacancy")
                                .getString("name")
                        if (items.getJSONObject(j).getJSONObject("vacancy")
                                .has("employer") && !items.getJSONObject(j)
                                .getJSONObject("vacancy").isNull("employer")
                        )
                            companyVacancy = items.getJSONObject(j).getJSONObject("vacancy")
                                .getJSONObject("employer").getString("name")
                    }
                    adapter.addSummary(Summary(nameVacancy ,
                        companyVacancy ,
                        idSummary ,
                        dateSummary ,
                        statusSummary ,
                        idVacancy
                    ))
                }
            } else binding.text.text = "Отправленных резюме не найдено"
        }

        binding.swipeRefresh.setOnRefreshListener {
            VolleyRequest(this).sendRequest(url , Request.Method.GET , null , true)
            dataModel.loading.value = true
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu , inflater: MenuInflater) {
        inflater.inflate(R.menu.logout_with_delete , menu)
        super.onCreateOptionsMenu(menu , inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                UsageDB(MainActivity.getContext()).deleteDB(null , null)
                MainActivity.checkUser(true)
                return true
            }
            R.id.deleteAll -> {
                val dialog = AlertDialog.Builder(this.context)
                dialog.setCancelable(true)
                    .setTitle("Вы уверены, что хотите удалить все отправленные резюме?")
                    .setPositiveButton("Да, я хочу удалить!") { _ , _ ->
                        Snackbar.make(binding.root ,
                            "Все отправленные резюме сейчас удалятся" ,
                            Snackbar.LENGTH_LONG).show()
                        VolleyRequest(this).sendRequest("$url/delete" , Request.Method.GET , null , true)
                        dataModel.loading.value = true
                    }
                    .setNegativeButton("Отмена") { dialogAlert , _ ->
                        dialogAlert.cancel()
                    }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(vacancy: Summary) {
        VolleyRequest(this).sendRequest(url + "/delete/" + vacancy.idSummary ,
            Request.Method.GET ,
            null ,
            true)
        dataModel.loading.value = true
    }

    override fun onClickItem(vacancy: Summary) {
        if (vacancy.urlVacancy != null) {
            val url = "https://thisistesttoo.000webhostapp.com/api/vacancy/" + vacancy.urlVacancy
            dataModelVacancy.getVacancy(url)
            DialogVacancyFragment().show((activity as MainActivity).supportFragmentManager ,
                "MyCustomFragment")
        } else Snackbar.make(binding.root , "Не удалось найти вакансию" , Snackbar.LENGTH_LONG)
            .show()

    }

    override fun onResponse(response: JSONObject) {
        when {
            response.has("errors") -> Toast.makeText(MainActivity.getContext() ,
                response.getString("errors") ,
                Toast.LENGTH_LONG).show()

            response.has("items") -> dataModel.summaries.value = response
            else -> Toast.makeText(MainActivity.getContext() ,
                "Не предвиденная ошибка" ,
                Toast.LENGTH_LONG).show()
        }
        dataModel.loading.value = false
    }

    override fun onError(response: VolleyError) {
        var error = "Ошибка подключения к серверу, попробуйте еще раз позже"
        if (response.networkResponse?.statusCode == 401) {
            UsageDB(MainActivity.getContext()).deleteDB(null , null)
            MainActivity.checkUser(true)
            error="Вход в аккаунт больше не действителен"
        }
        Toast.makeText(this.context ,
            error , Toast.LENGTH_LONG).show()
        dataModel.loading.value = false
    }
}
