package com.solidscorpion.medic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.solidscorpion.medic.pojo.SearchResultItem

class SearchAutocomplete(context: Context) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)
    val searchResults : MutableList<Map<String, SearchResultItem>> = mutableListOf()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return View(layoutInflater.context)
    }

    override fun getItem(position: Int): Any {
        return Any()
    }

    override fun getItemId(position: Int): Long {
        return -1
    }

    override fun getCount(): Int {
        return 0
    }

}