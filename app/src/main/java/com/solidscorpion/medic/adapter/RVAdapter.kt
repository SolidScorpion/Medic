package com.solidscorpion.medic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solidscorpion.medic.R
import com.solidscorpion.medic.pojo.ModelMenuItem


class RVAdapter(context: Context, val data: List<ModelMenuItem>, val onClick: (ModelMenuItem) -> Unit) :
    RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item, parent, false)
        return ViewHolder(view).also {
            it.myTextView.setOnClickListener { _ ->
                onClick(data[it.adapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data[position]
        holder.myTextView.text = model.title
    }

    override fun getItemCount() = data.size


    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myTextView: TextView = itemView.findViewById(R.id.tvTitle)
    }

}