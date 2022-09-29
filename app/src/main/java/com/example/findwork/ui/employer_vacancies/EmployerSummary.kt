package com.example.findwork.ui.employer_vacancies

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.findwork.R
import com.example.findwork.databinding.FragmentEmployerSummaryBinding
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EmployerSummary : DialogFragment() {

    private val dataModel: EmployerVacanciesViewModel by activityViewModels()
    lateinit var binding: FragmentEmployerSummaryBinding

    @SuppressLint("SetTextI18n" , "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = FragmentEmployerSummaryBinding.inflate(inflater , container , false)
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_dialog)

        binding.apply {
            closeButton.setOnClickListener {
                dialog!!.cancel()
            }

            dataModel.summary.observe(viewLifecycleOwner) {
                summaryDate.text = if (!it.isNull("updated_at"))
                Instant.parse(it.getString("updated_at"))
                .atZone(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
                else "Не указано"
                summaryName.text = "Имя: " + if (!it.isNull("name")) it.getString("name") else "Не указано"
                summarySurname.text = "Фамилия: " +
                    if (!it.isNull("surname")) it.getString("surname") else "Не указано"
                summaryEducation.text = "Образование: " +
                    if (!it.isNull("education")) it.getString("education") else "Не указано"
                summaryDescription.text =
                    if (!it.isNull("about_myself"))
                        it.getString("about_myself")
                    else "Не указано"
                summaryEmail.text = "Почта: "+ if (!it.isNull("email")) it.getString("email") else "Не указано"
                summaryPhone.text = "Телефон: "+ if (!it.isNull("phone")) it.getString("phone") else "Не указано"
            }
            loading.visibility = View.GONE
            return root
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
//        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setLayout(width , ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.root.maxHeight = height
    }

}