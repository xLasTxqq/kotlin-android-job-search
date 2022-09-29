package com.example.findwork.ui.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.findwork.MainActivity

class Vp2Adapter(fragment: FragmentActivity, private val list: List<Fragment>):FragmentStateAdapter(fragment){
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}