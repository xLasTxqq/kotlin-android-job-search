package com.example.findwork.ui.vacancies

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.ArrayMap
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.databinding.VacanciesFragmentBinding
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONArray
import org.json.JSONObject

class VacanciesFragment : Fragment() , VacanciesRecyclerView.Listener , VolleyRequest.Listener {

    private val dataModel: VacanciesViewModel by activityViewModels()
    private lateinit var binding: VacanciesFragmentBinding
    private val adapter: VacanciesRecyclerView = VacanciesRecyclerView(this)
    private lateinit var navController: NavController

    private val url = "https://thisistesttoo.000webhostapp.com/api/vacancy"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = VacanciesFragmentBinding.inflate(inflater , container , false)
        setHasOptionsMenu(true)
        navController = findNavController()

        dataModel.loadingVacancy.observe(viewLifecycleOwner) {
            if (!it) {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.progressBar2.visibility = View.GONE
            } else {
                if (!binding.swipeRefresh.isRefreshing && binding.progressBar2.visibility == View.GONE)
                    binding.progressBar.visibility = View.VISIBLE
            }
        }

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(MainActivity.getContext())
            recyclerView.adapter = adapter
            swipeRefresh.setOnRefreshListener {
                dataModel.loadingVacancy.value = true
                VolleyRequest(this@VacanciesFragment).sendRequest(url ,
                    Request.Method.POST , if (dataModel.filters.value.isNullOrEmpty()) null
                    else JSONObject(dataModel.filters.value as Map<* , *>) ,
                    false)
            }
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView , dx: Int , dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    layoutManager.findFirstVisibleItemPosition()
                    super.onScrolled(recyclerView , dx , dy)
                    val body =
                        if (dataModel.filters.value != null) JSONObject(dataModel.filters.value as Map<* , *>) else JSONObject()
                    if (layoutManager.itemCount - layoutManager.findLastVisibleItemPosition() < 5 && dataModel.loadingVacancy.value == false && dataModel.response.value!!.getInt(
                            "page") + 1 <= dataModel.response.value!!.getInt("pages")
                    ) {
                        body.put("page" , dataModel.response.value!!.getInt("page") + 1)
//                        sendRequest(body , true)
                        binding.progressBar2.visibility = View.VISIBLE
                        dataModel.loadingVacancy.value = true
                        VolleyRequest(this@VacanciesFragment).sendRequest(url ,
                            Request.Method.POST ,
                            body ,
                            false)
                    }
                }
            })
        }

        if (dataModel.response.value == null) {
            dataModel.loadingVacancy.value = true
            VolleyRequest(this@VacanciesFragment).sendRequest(url ,
                Request.Method.POST , if (dataModel.filters.value.isNullOrEmpty()) null
                else JSONObject(dataModel.filters.value as Map<* , *>) ,
                false)
        }

        dataModel.response.observe(viewLifecycleOwner) {
            adapter.removeVacancy()
            val currency: ArrayMap<String , String> = ArrayMap<String , String>()
            currency["BYR"] = "бел. руб."
            currency["RUR"] = "руб."
            currency["UAH"] = "грн."
            currency["UZS"] = "сум"
            binding.text.text =
                "Найдено: ${it.getString("found")} вакансий"
            val items = it.getJSONArray("items")
            for (j in 0 until items.length()) {
                var price = ""
                if (items.getJSONObject(j).has("salary") && !items.getJSONObject(j)
                        .isNull("salary")
                ) {
                    if (items.getJSONObject(j).getJSONObject("salary")
                            .has("from") && !items.getJSONObject(j).getJSONObject("salary")
                            .isNull("from")
                    ) {
                        price += "От " + items.getJSONObject(j).getJSONObject("salary")
                            .getString("from") + " " +
                                currency.getOrDefault(items.getJSONObject(j).getJSONObject("salary")
                                    .getString("currency") ,
                                    items.getJSONObject(j).getJSONObject("salary")
                                        .getString("currency")) + "\n"
                    }
                    if (items.getJSONObject(j).getJSONObject("salary")
                            .has("to") && !items.getJSONObject(j).getJSONObject("salary")
                            .isNull("to")
                    ) {
                        price += "До " + items.getJSONObject(j).getJSONObject("salary")
                            .getString("to") + " " +
                                currency.getOrDefault(items.getJSONObject(j).getJSONObject("salary")
                                    .getString("currency") ,
                                    items.getJSONObject(j).getJSONObject("salary")
                                        .getString("currency")) + " "
                    }
                } else price = "Не указано"
                val date: String =
                    if (items.getJSONObject(j).has("created_at") && !items.getJSONObject(j)
                            .isNull("created_at")
                    ) {
                        if(items.getJSONObject(j).getString("created_at").contains('+'))
                            items.getJSONObject(j).getString("created_at").split('+')[0]+'Z'
                        else items.getJSONObject(j).getString("created_at")
                    } else "Не указано"

                val image: String? =
                    if (!items.getJSONObject(j).getJSONObject("employer")
                            .isNull("logo_urls") && items.getJSONObject(j).getJSONObject("employer")
                            .has("logo_urls")
                    ) {
                        when {
                            items.getJSONObject(j).getJSONObject("employer")
                                .getJSONObject("logo_urls").has("90") && !items.getJSONObject(j)
                                .getJSONObject("employer")
                                .getJSONObject("logo_urls").isNull("90") -> items.getJSONObject(j)
                                .getJSONObject("employer").getJSONObject("logo_urls")
                                .getString("90")
                            items.getJSONObject(j).getJSONObject("employer")
                                .getJSONObject("logo_urls").has("240") && !items.getJSONObject(j)
                                .getJSONObject("employer")
                                .getJSONObject("logo_urls").isNull("240") -> items.getJSONObject(j)
                                .getJSONObject("employer").getJSONObject("logo_urls")
                                .getString("240")
                            else -> items.getJSONObject(j).getJSONObject("employer")
                                .getJSONObject("logo_urls").getString("original")
                        }
                    } else null
                adapter.addVacancy(
                    Vacancy(
                        items.getJSONObject(j).getString("name") ,
                        price ,
                        date ,
                        image ,
                        if (items.getJSONObject(j).has("url")) items.getJSONObject(j)
                            .getString("url")
                        else "$url/" + items.getJSONObject(
                            j).getString("id")
                    )
                )
            }
            if (dataModel.positionRecyclerView.value != null)
                binding.recyclerView.scrollToPosition(dataModel.positionRecyclerView.value!!)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu , inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu , menu)
        super.onCreateOptionsMenu(menu , inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                navController.navigate(R.id.navigation_home)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
//        dataModel.positionRecyclerView.value = layoutManager.findFirstVisibleItemPosition()
        dataModel.positionRecyclerView.value =
            layoutManager.findFirstCompletelyVisibleItemPosition()
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
//        dataModel.positionRecyclerView.value = layoutManager.findFirstVisibleItemPosition()
        dataModel.positionRecyclerView.value =
            layoutManager.findFirstCompletelyVisibleItemPosition()
    }

    override fun onClick(vacancy: Vacancy) {
        dataModel.loading.value = true
        dataModel.getVacancy(vacancy.urlVacancy)
        DialogVacancyFragment().show((activity as MainActivity).supportFragmentManager ,
            "MyCustomFragment")
    }

    override fun onResponse(response: JSONObject) {
        val jsonObject: JSONArray
        if (binding.progressBar2.visibility == View.VISIBLE && dataModel.response.value != null) {
            jsonObject = dataModel.response.value!!.getJSONArray("items")
            for (i in 0 until response.getJSONArray("items").length()) {
                jsonObject.put(response.getJSONArray("items").getJSONObject(i))
            }
            dataModel.positionRecyclerView.value =
                (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            dataModel.response.value = response.put("items" , jsonObject)
        } else
            dataModel.response.value = response
        dataModel.loadingVacancy.value = false
    }

    override fun onError(response: VolleyError) {
        Toast.makeText(MainActivity.getContext() ,
            "Подключение в серверу отсутсвует" ,
            Toast.LENGTH_LONG).show()
        dataModel.loadingVacancy.value = false
    }
}

