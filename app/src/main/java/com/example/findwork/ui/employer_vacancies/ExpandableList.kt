package com.example.findwork.ui.employer_vacancies

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.core.content.ContextCompat
import com.example.findwork.R
import com.example.findwork.databinding.EmployerVacancyGroupBinding
import com.example.findwork.databinding.EmployerVacancyItemBinding
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExpandableList internal constructor(
        private val data: JSONObject,
        val listener: Listener
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.data.getJSONArray("items").getJSONObject(listPosition).getJSONArray("summary").getJSONObject(expandedListPosition)
    }
    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }
    @SuppressLint("SetTextI18n")
    override fun getChildView(
        listPosition: Int ,
        expandedListPosition: Int ,
        isLastChild: Boolean ,
        convertView: View? ,
        parent: ViewGroup ,
    ): View {
        val binding = EmployerVacancyItemBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.employer_vacancy_item, parent, false)!!)
        binding.textName.text = "Имя: "+(getChild(listPosition, expandedListPosition) as JSONObject).getString("name")
        binding.textSurname.text = "Фамилия: "+(getChild(listPosition, expandedListPosition) as JSONObject).getString("surname")
        binding.textEducation.text = "Образование: "+(getChild(listPosition, expandedListPosition) as JSONObject).getString("education")
        if( (getChild(listPosition, expandedListPosition) as JSONObject).getString("status")=="Одобрена"){
            binding.root.setBackgroundColor(
                ContextCompat.getColor(parent.context, R.color.light_green))
            binding.acceptButton.visibility = View.GONE
        }

        binding.rejectButton.isFocusable = false
        binding.acceptButton.isFocusable = false
        binding.rejectButton.setOnClickListener {
            listener.onChange((getChild(listPosition, expandedListPosition) as JSONObject).getString("id"),false)
        }
        binding.acceptButton.setOnClickListener {
            listener.onChange((getChild(listPosition, expandedListPosition) as JSONObject).getString("id"),true)
        }
        return binding.root
    }
    override fun getChildrenCount(listPosition: Int): Int {
        return this.data.getJSONArray("items").getJSONObject(listPosition).getJSONArray("summary").length()
    }
    override fun getGroup(listPosition: Int): Any {
        return this.data.getJSONArray("items").getJSONObject(listPosition)
    }
    override fun getGroupCount(): Int {
//        return this.titleList.size
        return this.data.getJSONArray("items").length()
    }
    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }
    @SuppressLint("NewApi")
    override fun getGroupView(
        listPosition: Int ,
        isExpanded: Boolean ,
        convertView: View? ,
        parent: ViewGroup ,
    ): View {
        val binding = EmployerVacancyGroupBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.employer_vacancy_group, parent, false)!!)
        binding.textName.text = (getGroup(listPosition) as JSONObject).getString("name")
        val time=Instant.parse((getGroup(listPosition) as JSONObject).getString("updated_at"))
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
        binding.textDate.text = time
        binding.deleteButton.isFocusable = false
        binding.updateButton.isFocusable = false

        binding.deleteButton.setOnClickListener {
            listener.onClick((getGroup(listPosition) as JSONObject).getString("id"))
        }
        binding.updateButton.setOnClickListener {
            listener.onUpdate(getGroup(listPosition) as JSONObject)
        }
        return binding.root
    }
    override fun hasStableIds(): Boolean {
        return false
    }
    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
    interface Listener{
        fun onClick(id:String)
        fun onChange(id:String, status:Boolean)
        fun onUpdate(vacancy: JSONObject)
    }
}