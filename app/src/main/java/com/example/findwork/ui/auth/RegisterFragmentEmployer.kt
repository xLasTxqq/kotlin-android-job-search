package com.example.findwork.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.FragmentRegisterEmployerBinding
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONObject

class RegisterFragmentEmployer : Fragment(),VolleyRequest.Listener {

    private lateinit var binding: FragmentRegisterEmployerBinding
    private val dataModel: DataAuthViewModel by activityViewModels()
    private val url: String = "https://thisistesttoo.000webhostapp.com/api/register/employer"

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentRegisterEmployerBinding.inflate(inflater , container , false)

        binding.apply {

            dataModel.bodyRequest.observe(viewLifecycleOwner) {
                if (it.has(url)) {
                    name.setText(it.getJSONObject(url).getString("name"))
                    surname.setText(it.getJSONObject(url).getString("surname"))
                    phone.setText(it.getJSONObject(url).getString("phone"))
                    email.setText(it.getJSONObject(url).getString("email"))
                    company.setText(it.getJSONObject(url).getString("company"))
                    password.setText(it.getJSONObject(url).getString("password"))
                    confirmPassword.setText(it.getJSONObject(url)
                        .getString("password_confirmation"))
                }
            }
            confirmPassword.setOnEditorActionListener { _ , _ , _ ->
                if (submit.isEnabled) submit.callOnClick()
                false
            }
            dataModel.loading.observe(viewLifecycleOwner) {
                if (it == true) {
                    loading.visibility = View.VISIBLE
                    submit.isEnabled = false
                } else {
                    loading.visibility = View.GONE
                    validate()
                    if (dataModel.bodyRequest.value == null || !dataModel.bodyRequest.value!!.has(
                            url)
                    ) {
                        name.error = null
                        surname.error = null
                        phone.error = null
                        email.error = null
                        company.error = null
                        password.error = null
                        confirmPassword.error = null
                    }
                }
            }
            binding.switchLogin.setOnClickListener {
                findNavController().navigate(R.id.navigation_register)
            }
            submit.setOnClickListener {
                dataModel.loading.value = true
                updateBodyRequest()
                VolleyRequest(this@RegisterFragmentEmployer).sendRequest(url, Request.Method.POST, dataModel.bodyRequest.value?.getJSONObject(url), false)
            }

            name.addTextChangedListener { validate() }
            surname.addTextChangedListener { validate() }
            phone.addTextChangedListener { validate() }
            email.addTextChangedListener { validate() }
            company.addTextChangedListener { validate() }
            password.addTextChangedListener { validate() }
            confirmPassword.addTextChangedListener { validate() }
        }

        return binding.root
    }

    private fun validate() {
        if (binding.name.text.isBlank()) {
            binding.name.error = "Имя не может быть пустым"
            binding.submit.isEnabled = false
        } else if (binding.surname.text.isBlank()) {
            binding.surname.error = "Фамилия не может быть пустой"
            binding.submit.isEnabled = false
        } else if (!Patterns.PHONE.matcher(binding.phone.text).matches()) {
            binding.phone.error = "Не валидный телефон"
            binding.submit.isEnabled = false
        } else if (binding.phone.text.isBlank()) {
            binding.phone.error = "Телефон не может быть пустым"
            binding.submit.isEnabled = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text).matches()) {
            binding.email.error = "Не валидная почта"
            binding.submit.isEnabled = false
        } else if (binding.email.text.isBlank()) {
            binding.email.error = "Почта не может быть пустой"
            binding.submit.isEnabled = false
        } else if (binding.company.text.isBlank()) {
            binding.company.error = "Компания не может быть пустой"
            binding.submit.isEnabled = false
        } else if (binding.password.text.length < 8) {
            binding.password.error = "Длинна пароля должна быть >=8 символов"
            binding.submit.isEnabled = false

        } else if (binding.password.text.isBlank()) {
            binding.password.error = "Пароль не может быть пустым"
            binding.submit.isEnabled = false

        } else if (binding.confirmPassword.text.isBlank()) {
            binding.confirmPassword.error = "Подтверждение пароля не может быть пустым"
            binding.submit.isEnabled = false

        } else if (binding.confirmPassword.text.toString() != binding.password.text.toString()) {
            binding.confirmPassword.error = "Подтверждение пароля должно совпадать с паролем"
            binding.submit.isEnabled = false
        } else {
            binding.submit.isEnabled = dataModel.loading.value == false
            binding.name.error = null
            binding.surname.error = null
            binding.phone.error = null
            binding.email.error = null
            binding.company.error = null
            binding.password.error = null
            binding.confirmPassword.error = null
        }
    }

    private fun updateBodyRequest() {
        if (binding.name.text.isNotEmpty() || binding.surname.text.isNotEmpty()
            || binding.phone.text.isNotEmpty() || binding.email.text.isNotEmpty()
            || binding.company.text.isNotEmpty() || binding.password.text
                .isNotEmpty() || binding.confirmPassword.text.isNotEmpty()
        )
            if (dataModel.bodyRequest.value == null)
                dataModel.bodyRequest.value =
                    JSONObject().put(url ,
                        JSONObject()
                            .put("name" , binding.name.text)
                            .put("surname" , binding.surname.text)
                            .put("phone" , binding.phone.text)
                            .put("email" , binding.email.text)
                            .put("company" , binding.company.text)
                            .put("password" , binding.password.text)
                            .put("password_confirmation" , binding.confirmPassword.text))
            else dataModel.bodyRequest.value =
                dataModel.bodyRequest.value!!.put(url ,
                    JSONObject()
                        .put("name" , binding.name.text)
                        .put("surname" , binding.surname.text)
                        .put("phone" , binding.phone.text)
                        .put("email" , binding.email.text)
                        .put("company" , binding.company.text)
                        .put("password" , binding.password.text)
                        .put("password_confirmation" , binding.confirmPassword.text))
        else dataModel.bodyRequest.value?.remove(url)
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
        when{
            response.has("errors")->Toast.makeText(this.context,"Не правильная почта или пароль",Toast.LENGTH_LONG).show()
            response.has("token") -> {
                UsageDB(MainActivity.getContext()).insertDB(response.getString("token"),1)
                MainActivity.checkUser(true)
                Toast.makeText(this.context,"Вы успешно вошли в аккаунт!",Toast.LENGTH_LONG).show()
            }
            else -> Toast.makeText(this.context,"Не предвиденная ошибка",Toast.LENGTH_LONG).show()
        }
        dataModel.loading.value=false
    }

    override fun onError(response: VolleyError) {
        Toast.makeText(this.context,"Ошибка подключения к серверу, попробуйте еще раз позже",Toast.LENGTH_LONG).show()
        dataModel.loading.value=false
    }
}