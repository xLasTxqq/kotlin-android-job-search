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
import com.example.findwork.databinding.FragmentLoginBinding
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONObject

class LoginFragment : Fragment(),VolleyRequest.Listener {

    private lateinit var binding: FragmentLoginBinding
    private val dataModel: DataAuthViewModel by activityViewModels()
    private val url: String = "https://thisistesttoo.000webhostapp.com/api/login"

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater , container , false)

        binding.apply {
            dataModel.bodyRequest.observe(viewLifecycleOwner) {
                if (it.has(url)) {
                    email.setText(it.getJSONObject(url).getString("email"))
                    password.setText(it.getJSONObject(url).getString("password"))
                }
            }
            password.setOnEditorActionListener { _ , _ , _ ->
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
                        binding.email.error = null
                        binding.password.error = null
                    }
                }
            }
            binding.switchLogin.setOnClickListener {
                findNavController().navigate(R.id.navigation_login_employer)
            }
            submit.setOnClickListener {
                dataModel.loading.value = true
                updateBodyRequest()
                VolleyRequest(this@LoginFragment).sendRequest(url, Request.Method.POST, dataModel.bodyRequest.value?.getJSONObject(url), false)
            }

            email.addTextChangedListener { validate() }
            password.addTextChangedListener { validate() }
        }

        return binding.root
    }

    private fun validate() {
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text).matches()) {
            binding.email.error = "Не валидная почта"
            binding.submit.isEnabled = false
        } else if (binding.email.text.isBlank()) {
            binding.email.error = "Почта не может быть пустой"
            binding.submit.isEnabled = false
        } else if (binding.password.text.length <= 5) {
            binding.password.error = "Длинна пароля должна быть >5 символов"
            binding.submit.isEnabled = false

        } else if (binding.password.text.isBlank()) {
            binding.password.error = "Пароль не может быть пустым"
            binding.submit.isEnabled = false
        } else {
            binding.submit.isEnabled = dataModel.loading.value == false
            binding.email.error = null
            binding.password.error = null
        }
    }

    private fun updateBodyRequest() {
        if (binding.email.text.isNotEmpty() || binding.password.text.isNotEmpty())
            if (dataModel.bodyRequest.value == null)
                dataModel.bodyRequest.value =
                    JSONObject().put(url ,
                        JSONObject().put("email" , binding.email.text)
                            .put("password" , binding.password.text))
            else dataModel.bodyRequest.value =
                dataModel.bodyRequest.value!!.put(url ,
                    JSONObject().put("email" , binding.email.text)
                        .put("password" , binding.password.text))
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
            response.has("errors")->{
                Toast.makeText(this.context,"Не правильная почта или пароль",Toast.LENGTH_LONG).show()
                binding.password.setText("")
            }
            response.has("token") -> {
                UsageDB(MainActivity.getContext()).insertDB(response.getString("token"),0)
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