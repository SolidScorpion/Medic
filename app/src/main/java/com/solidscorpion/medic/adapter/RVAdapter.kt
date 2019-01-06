package com.solidscorpion.medic.adapter

import android.content.Context
import android.graphics.Typeface
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
    private var sepCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(viewType, parent, false)
        return ViewHolder(view).also {
            it.myTextView?.setOnClickListener { _ ->
                val modelMenuItem = data[it.adapterPosition]
                if (modelMenuItem.title.length != 1) {
                    onClick(modelMenuItem)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (data[position].title.length == 1) {
            return R.layout.separator
        } else if (data[position].title.length == 2){
            return R.layout.copyright_item
        }
        return R.layout.item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data[position]
        if (getItemViewType(position) == R.layout.separator) sepCount++
        if (getItemViewType(position) == R.layout.item) {
            if (sepCount < 2) {
                holder.myTextView?.text = model.title.toUpperCase()
                holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                        "fonts/IBMPlexSans-Bold.ttf")
                holder.myTextView?.textSize = 15F
            } else {
                holder.myTextView?.text = model.title
                holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                        "fonts/IBMPlexSans-Text.ttf")
                holder.myTextView?.textSize = 14F
            }
        } else if (getItemViewType(position) == R.layout.copyright_item) {
            holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                    "fonts/IBMPlexSans-Text.ttf")
            holder.myTextView?.textSize = 12F
            sepCount = 0
        }
    }

    override fun getItemCount() = data.size

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myTextView: TextView? = itemView.findViewById(R.id.tvTitle)
    }

}