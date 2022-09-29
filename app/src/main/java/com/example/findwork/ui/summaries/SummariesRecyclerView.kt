package com.example.findwork.ui.summaries

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.findwork.R
import com.example.findwork.databinding.SummaryItemBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SummariesRecyclerView(val listener:Listener): RecyclerView.Adapter<SummariesRecyclerView.VacanciesHolder>() {
    private var ticketList=ArrayList<Summary>()

    class VacanciesHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = SummaryItemBinding.bind(item)
        @SuppressLint("NewApi")
        fun bind(vacancy: Summary , listener:Listener) = with(binding){
            vacancy.apply {
                textName.text=nameVacancy
                textCompany.text=companyVacancy
                textTimeStamp.text= Instant.parse(dateSummary)
                    .atZone(ZoneId.of("Europe/Moscow"))
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
                if(statusSummary=="Отклонена")
                    textStatus.setTextColor(Color.RED)
                else if(statusSummary == "Одобрена")
                    textStatus.setTextColor(Color.GREEN)
                textStatus.text = statusSummary
            }
            binding.deleteSummary.setOnClickListener {
                listener.onClick(vacancy)
            }
            binding.itemSummary.setOnClickListener {
                listener.onClickItem(vacancy)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): VacanciesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.summary_item, parent, false)
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
    fun addSummary(vacancy: Summary){
        ticketList.add(vacancy)
        notifyDataSetChanged()
    }
    fun removeSummary(){
        ticketList=ArrayList()
    }
    interface Listener{
        fun onClick(vacancy: Summary)
        fun onClickItem(vacancy: Summary)
    }
}
data class Summary(
    val nameVacancy: String,
    val companyVacancy: String,
    val idSummary: String,
    val dateSummary: String,
    val statusSummary: String,
    val urlVacancy: String?
)
