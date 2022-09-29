package com.example.findwork.ui.vacancies

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.VolleyError
import com.example.findwork.MainActivity
import com.example.findwork.R
import com.example.findwork.UsageDB
import com.example.findwork.databinding.DialogVacancyFragmentBinding
import com.example.findwork.ui.data.VolleyRequest
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class DialogVacancyFragment : DialogFragment() , VolleyRequest.Listener {
    lateinit var binding: DialogVacancyFragmentBinding
    private val dataModel: VacanciesViewModel by activityViewModels()

    @SuppressLint("SetTextI18n" , "SetJavaScriptEnabled" , "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle? ,
    ): View {
        binding = DialogVacancyFragmentBinding.inflate(inflater , container , false)
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_dialog)

        if (UsageDB(MainActivity.getContext()).readDB(null ,
                null ,
                null ,
                "id" ,
                0).size < 1 || UsageDB(MainActivity.getContext()).readDB(null ,
                null ,
                null ,
                "id" ,
                0)[0][2].toInt() != 0
        ) {
            binding.buttonSendSummary.visibility = View.GONE
        }

        dataModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                binding.loading.visibility = View.VISIBLE
                binding.buttonSendSummary.isEnabled = false
            } else {
                binding.loading.visibility = View.GONE
                binding.buttonSendSummary.isEnabled = true
            }
        }

        binding.closeButton.setOnClickListener {
            dialog!!.cancel()
        }

        binding.buttonSendSummary.setOnClickListener {
            if (dataModel.singleVacancyResponse.value != null)
                if (dataModel.singleVacancyResponse.value!!.isNull("alternate_url"))
//                    sendSummary(dataModel.singleVacancyResponse.value!!.getString("id"))
                    VolleyRequest(this)
                        .sendRequest("https://thisistesttoo.000webhostapp.com/api/summary/" +
                                dataModel.singleVacancyResponse.value!!.getString("id") ,
                            Request.Method.POST ,
                            null ,
                            true)
                else {
                    binding.scrollView2.visibility = View.GONE
                    binding.webView.webViewClient = MyWebViewClient()
                    binding.webView.visibility = View.VISIBLE
                    binding.webView.settings.javaScriptEnabled = true
                    binding.webView.loadUrl(dataModel.singleVacancyResponse.value!!.getString("alternate_url"))
                }

        }

        dataModel.singleVacancyResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                val currency: ArrayMap<String , String> = ArrayMap<String , String>()
                currency["BYR"] = "бел. руб."
                currency["RUR"] = "руб."
                currency["UAH"] = "грн."
                currency["UZS"] = "сум"

                binding.apply {
                    binding.vacancyImage.setImageResource(0)
                    if (it.has("name") && !it.isNull("name"))
                        vacancyName.text = it.getString("name")
                    if (it.has("salary") && !it.isNull("salary")) {
                        vacancySalary.text = "Доход: "
                        if (!it.getJSONObject("salary").isNull("from"))
                            vacancySalary.text = vacancySalary.text.toString() +
                                    "от " + it.getJSONObject("salary").getString("from") + " "
                        if (!it.getJSONObject("salary").isNull("to"))
                            vacancySalary.text = vacancySalary.text.toString() +
                                    "до " + it.getJSONObject("salary").getString("to") + " "
                        if (!it.getJSONObject("salary").isNull("currency"))
                            vacancySalary.text =
                                vacancySalary.text.toString() + currency.getOrDefault(it.getJSONObject(
                                    "salary")
                                    .getString("currency") ,
                                    it.getJSONObject("salary")
                                        .getString("currency"))
                    } else vacancySalary.text = "Доход: не указано"
                    if (it.has("area") && !it.isNull("area"))
                        vacancyArea.text = "Регион: " + it.getJSONObject("area").getString("name")
                    if (it.has("description") && !it.isNull("description"))
                        vacancyDescription.text = Html.fromHtml(it.getString("description"))
                    if (it.has("created_at") && !it.isNull("created_at"))
//                        vacancyDate.text = it.getString("created_at").split("T" , " ")[0]
                        vacancyDate.text = Instant.parse(
                            if(it.getString("created_at").contains('+'))
                                it.getString("created_at").split('+')[0]+'Z'
                            else it.getString("created_at")
                        )
                            .atZone(ZoneId.of("Europe/Moscow"))
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
                    //TODO date
                    if (it.has("employer") && !it.isNull("employer")) {
                        if (it.getJSONObject("employer")
                                .has("name") && !it.getJSONObject("employer").isNull("name")
                        )
                            vacancyCompany.text =
                                "Компания: " + it.getJSONObject("employer").getString("name")
                        if (it.getJSONObject("employer")
                                .has("logo_urls") && !it.getJSONObject("employer")
                                .isNull("logo_urls")
                        )
                            when {
                                it.getJSONObject("employer")
                                    .getJSONObject("logo_urls")
                                    .has("90") && !it.getJSONObject("employer")
                                    .getJSONObject("logo_urls").isNull("90") -> DownloadImageTask(
                                    vacancyImage).execute(it
                                    .getJSONObject("employer").getJSONObject("logo_urls")
                                    .getString("90"))
                                it.getJSONObject("employer")
                                    .getJSONObject("logo_urls")
                                    .has("240") && !it.getJSONObject("employer")
                                    .getJSONObject("logo_urls").isNull("240") -> DownloadImageTask(
                                    vacancyImage).execute(it
                                    .getJSONObject("employer").getJSONObject("logo_urls")
                                    .getString("240"))
                                it.getJSONObject("employer")
                                    .getJSONObject("logo_urls")
                                    .has("240") && !it.getJSONObject("employer")
                                    .getJSONObject("logo_urls").isNull("original") -> DownloadImageTask(
                                    vacancyImage).execute(it
                                    .getJSONObject("employer").getJSONObject("logo_urls")
                                    .getString("original"))
//                                else -> DownloadImageTask(vacancyImage).execute(it.getJSONObject("employer")
//                                    .getJSONObject("logo_urls").getString("original"))
                            }
                    }
                    if (it.has("experience") && !it.isNull("experience"))
                        vacancyExperience.text =
                            "Опыт: " + it.getJSONObject("experience").getString("name")
                    else vacancyExperience.text = "Опыт: не указан"
                    if (it.has("schedule") && !it.isNull("schedule"))
                        vacancySchedule.text =
                            "График: " + it.getJSONObject("schedule").getString("name")
                    else vacancySchedule.text = "График: не указан"
                    if (it.has("contacts") && !it.isNull("contacts")) {
                        if (it.getJSONObject("contacts").has("name"))
                            vacancyContactName.text =
                                "Имя: " + it.getJSONObject("contacts").getString("name")
                        if (it.getJSONObject("contacts").has("email"))
                            vacancyContactName.text =
                                "Почта: " + it.getJSONObject("contacts").getString("email")
                        if (it.getJSONObject("contacts").has("phone"))
                            vacancyContactName.text =
                                "Телефон: " + it.getJSONObject("contacts").getString("phone")
                    } else vacancyContacts.visibility = View.GONE
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.layot.setPadding(0,0,0,(resources.displayMetrics.widthPixels * 0.30).toInt())
//        dialog!!.window?.setLayout(width , height)
        binding.root.maxHeight = height
    }

//TODO Поменять метод загрузки картинки
    private class DownloadImageTask(imageView: ImageView) :
        AsyncTask<String? , Void? , Bitmap?>() {
        @SuppressLint("StaticFieldLeak")
        val bmImage: ImageView = imageView

        override fun doInBackground(vararg urls: String?): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val server: InputStream = URL(urldisplay).openStream()
//            BitmapFactory.Options().inJustDecodeBounds = true
                mIcon11 = BitmapFactory.decodeStream(server)

            } catch (e: Exception) {
                println("Ошибка")
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            bmImage.visibility = View.VISIBLE
            bmImage.setImageResource(0)
            bmImage.setImageBitmap(result)
        }
    }

    private class MyWebViewClient : WebViewClient() {
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView ,
            request: WebResourceRequest ,
        ): Boolean {
            view.loadUrl(request.url.toString())
            return true
        }

        // Для старых устройств
        override fun shouldOverrideUrlLoading(view: WebView , url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    override fun onResponse(response: JSONObject) {
        when {
            response.has("id") -> Toast.makeText(MainActivity.getContext() ,
                "Заявка отправлена" ,
                Toast.LENGTH_LONG).show()
            response.has("errors") -> Toast.makeText(MainActivity.getContext() ,
                response.getString("errors") ,
                Toast.LENGTH_LONG).show()
            else -> Toast.makeText(MainActivity.getContext() ,
                "Произошла не предвиденная ошибка" ,
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onError(response: VolleyError) {
        Toast.makeText(this.context ,
            "Подключение в серверу отсутсвует" ,
            Toast.LENGTH_LONG).show()
    }
}
