package com.solidscorpion.medic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable

class SearchAutocomplete(context: Context) : BaseAdapter(), Filterable {
    private val layoutInflater = LayoutInflater.from(context)
    private val filter = AutocompleteFilter()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return View(layoutInflater.context)
    }

    override fun getItem(position: Int): Any {
        return Any()
    }

    override fun getItemId(position: Int): Long {
        return  -1
    }

    override fun getCount(): Int {
        return  -1
    }

    override fun getFilter() = filter

    class AutocompleteFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults()
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        }
    }
}