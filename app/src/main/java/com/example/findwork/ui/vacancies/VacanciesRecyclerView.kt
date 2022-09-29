package com.example.findwork.ui.vacancies

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.findwork.R
import com.example.findwork.databinding.VacanciesFragmentBinding
import com.example.findwork.databinding.VacancyItemBinding
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class VacanciesRecyclerView(val listener:Listener): RecyclerView.Adapter<VacanciesRecyclerView.VacanciesHolder>() {
    var ticketList=ArrayList<Vacancy>()

    class VacanciesHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = VacancyItemBinding.bind(item)
        @SuppressLint("NewApi")
        fun bind(vacancy: Vacancy , listener:Listener) = with(binding){
            vacancy.apply {
                textName.text=nameVacancy
                textPrice.text=priceVacancy
                textDate.text= Instant.parse(dateVacancy)
                    .atZone(ZoneId.of("Europe/Moscow"))
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
                imageView.setImageResource(0)
                if(!imageVacancy.isNullOrEmpty()) {
                    DownloadImageTask(imageView).execute(imageVacancy)
                }
            }
            binding.itemVacancy.setOnClickListener {
                listener.onClick(vacancy)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): VacanciesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vacancy_item, parent, false)
        return VacanciesHolder(view)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: VacanciesHolder, position: Int) {
        holder.bind(ticketList[position],listener)
    }
    override fun getItemCount(): Int {
        return ticketList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addVacancy(vacancy: Vacancy){
        ticketList.add(vacancy)
        notifyDataSetChanged()
    }
    fun removeVacancy(){
        ticketList=ArrayList<Vacancy>()
    }
    interface Listener{
        fun onClick(vacancy: Vacancy)
    }
}
data class Vacancy(
    val nameVacancy: String,
    val priceVacancy: String,
    val dateVacancy: String,
    val imageVacancy: String?,
    val urlVacancy: String
)
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
        bmImage.setImageBitmap(result)
    }
}
