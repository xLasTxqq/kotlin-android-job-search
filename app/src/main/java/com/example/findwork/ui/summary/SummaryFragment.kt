package com.example.findwork.ui.summary

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.FragmentSummaryBinding
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONObject

class SummaryFragment : Fragment() , VolleyRequest.Listener {

    private lateinit var binding: FragmentSummaryBinding
    private val dataModel: SummaryViewModel by activityViewModels()
    private val url: String = "https://thisistesttoo.000webhostapp.com/api/summary"
    private val educationList =
        arrayListOf("Без образования" , "Среднее" , "Среднее профессиональное" , "Высшее")
    private var body:JSONObject? = null

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentSummaryBinding.inflate(inflater , container , false)
        setHasOptionsMenu(true)
        binding.apply {

            listEducation.adapter = ArrayAdapter(MainActivity.getContext() ,
                android.R.layout.simple_list_item_multiple_choice ,
                educationList)
            listEducation.setItemChecked(0 , true)

            if (dataModel.summary.value == null) {
                updateSummary()
            }

            swipeRefresh.setOnRefreshListener {
                updateSummary()
            }

            dataModel.summary.observe(viewLifecycleOwner) {
                if (it.length() > 0) {
                    inputName.setText(if (it.isNull("name")) "" else it.getString("name"))
                    inputSurname.setText(if (it.isNull("surname")) "" else it.getString("surname"))
                    inputEmail.setText(if (it.isNull("email")) "" else it.getString("email"))
                    inputPhone.setText(if (it.isNull("phone")) "" else it.getString("phone"))
                    listEducation.setItemChecked(educationList.indexOf(it.getString("education")) ,
                        true)
                    inputAboutSelf.setText(if (it.isNull("about_myself")) "" else it.getString("about_myself"))
                }
            }
            inputAboutSelf.setOnEditorActionListener { _ , _ , _ ->
                if (submit.isEnabled) submit.callOnClick()
                false
            }
            dataModel.loading.observe(viewLifecycleOwner) {
                if (it == true) {
                    if (!swipeRefresh.isRefreshing)
                        loading.visibility = View.VISIBLE
                    submit.isEnabled = false
                    if (!swipeRefresh.isRefreshing)
                        swipeRefresh.isEnabled = false
                } else {
                    loading.visibility = View.INVISIBLE
                    swipeRefresh.isRefreshing = false
                    swipeRefresh.isEnabled = true
                    validate()
                    if (dataModel.summary.value == null || dataModel.summary.value!!.length() < 1) {
                        inputName.error = null
                        inputSurname.error = null
                        inputEmail.error = null
                        inputPhone.error = null
                        inputAboutSelf.error = null
                    }
                }
            }
            submit.setOnClickListener {
                dataModel.loading.value = true
                updateBodyRequest()
                VolleyRequest(this@SummaryFragment).sendRequest(url ,
                    Request.Method.POST ,
                    dataModel.summary.value ,
                    true)
                body = dataModel.summary.value
//                dataModel.sendRequest(urlCreate)
            }

            dataModel.summaryRequest.observe(viewLifecycleOwner) {
                validate()
            }

            inputName.addTextChangedListener {
                validate()
            }
            inputSurname.addTextChangedListener {
                validate()
            }
            inputEmail.addTextChangedListener {
                validate()
            }
            inputPhone.addTextChangedListener {
                validate()
            }
            inputAboutSelf.addTextChangedListener {
                validate()
            }
            listEducation.onItemClickListener = AdapterView.OnItemClickListener { _ , _ , _ , _ ->
                validate()
            }
        }

        return binding.root
    }

    private fun updateSummary() {
        dataModel.loading.value = true
        VolleyRequest(this).sendRequest(url , Request.Method.GET , null , true)
//        dataModel.sendRequest(urlUpdate)
    }

    private fun validate() {
        binding.apply {
            if (inputName.text.isBlank()) {
                inputName.error = "Имя не может быть пустым"
                binding.submit.isEnabled = false
            } else if (inputSurname.text.isBlank()) {
                inputSurname.error = "Фамилия не может быть пустой"
                binding.submit.isEnabled = false
            } else if (inputPhone.text.isBlank() && inputEmail.text.isBlank()) {
                inputPhone.error = "Укажите как с вами связаться"
                inputEmail.error = "Укажите как с вами связаться"
                binding.submit.isEnabled = false
            } else if (inputEmail.text.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(inputEmail.text)
                    .matches()
            ) {
                inputEmail.error = "Не валидная почта"
                binding.submit.isEnabled = false
            } else if (inputPhone.text.isNotBlank() && !Patterns.PHONE.matcher(inputPhone.text)
                    .matches()
            ) {
                inputPhone.error = "Не валидный телефон"
                binding.submit.isEnabled = false
            } else if (checkedUpdate()) {
                submit.isEnabled = dataModel.loading.value == false
                inputName.error = null
                inputSurname.error = null
                inputEmail.error = null
                inputPhone.error = null
            } else submit.isEnabled = false
        }
    }

    private fun checkedUpdate(): Boolean {
        return (binding.inputName.text.toString() != if (dataModel.summaryRequest.value?.isNull("name") == true) ""
        else dataModel.summaryRequest.value?.getString("name")) ||
                (binding.inputSurname.text.toString() != if (dataModel.summaryRequest.value?.isNull(
                        "surname") == true
                ) ""
                else dataModel.summaryRequest.value?.getString("surname")) ||
                (binding.inputPhone.text.toString() != if (dataModel.summaryRequest.value?.isNull("phone") == true) ""
                else dataModel.summaryRequest.value?.getString("phone")) ||
                (binding.inputEmail.text.toString() != if (dataModel.summaryRequest.value?.isNull("email") == true) ""
                else dataModel.summaryRequest.value?.getString("email")) ||
                educationList[binding.listEducation.checkedItemPosition] != dataModel.summaryRequest.value?.getString(
            "education") ||
                (binding.inputAboutSelf.text.toString() != if (dataModel.summaryRequest.value?.isNull(
                        "about_myself") == true
                ) ""
                else dataModel.summaryRequest.value?.getString("about_myself"))
    }

    private fun updateBodyRequest() {
        if (binding.inputName.text.isNotEmpty() || binding.inputSurname.text
                .isNotEmpty() || binding.inputEmail.text.isNotEmpty()
            || binding.inputPhone.text.isNotEmpty()
            || binding.inputAboutSelf.text.isNotEmpty()
            || (educationList[binding.listEducation.checkedItemPosition] != if (dataModel.summaryRequest.value?.isNull(
                    "education") == true
            ) educationList[0]
            else dataModel.summaryRequest.value?.getString("education"))
        )
            dataModel.summary.value =
                JSONObject().put("name" , binding.inputName.text)
                    .put("surname" , binding.inputSurname.text)
                    .put("email" , binding.inputEmail.text)
                    .put("phone" , binding.inputPhone.text)
                    .put("education" , educationList[binding.listEducation.checkedItemPosition])
                    .put("about_myself" , binding.inputAboutSelf.text)
        else dataModel.summary.value = JSONObject()
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

    override fun onPause() {
        super.onPause()
        updateBodyRequest()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateBodyRequest()
    }

    override fun onResponse(response: JSONObject) {
        when {
            response.has("name") -> {
                dataModel.summary.value = response
                dataModel.summaryRequest.value = response
            }
            response.has("id") -> {
                dataModel.summaryRequest.value = body
                Toast.makeText(MainActivity.getContext() ,
                    "Резюме сохранено" ,
                    Toast.LENGTH_LONG).show()
            }
            response.has("errors") -> {
                println(response.getString("errors"))
                Toast.makeText(this.context ,
                    response.getString("errors") ,
                    Toast.LENGTH_LONG).show()
            }
            response.length() < 1 -> {
                dataModel.summary.value = JSONObject()
                dataModel.summaryRequest.value = JSONObject()
            }
            else -> {
                Toast.makeText(this.context ,
                    "Произошла не предвиденная ошибка" ,
                    Toast.LENGTH_LONG).show()
            }
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