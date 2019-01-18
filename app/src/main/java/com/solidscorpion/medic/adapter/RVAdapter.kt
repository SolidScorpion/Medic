package com.solidscorpion.medic.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.solidscorpion.medic.R
import com.solidscorpion.medic.pojo.ModelMenuItem


class RVAdapter(context: Context, val data: List<ModelMenuItem>, val onClick: (ModelMenuItem) -> Unit, val onSignUpClick: (String) -> Unit) :
    RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(viewType, parent, false)
        return ViewHolder(view).also {
            it.myTextView?.setOnClickListener { _ ->
                val modelMenuItem = data[it.adapterPosition]
                if (modelMenuItem.title.length != 1) {
                    onClick(modelMenuItem)
                }
            }
            it.btnSubscribe?.setOnClickListener { _ ->
                onSignUpClick("")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when {
            data[position].title.isEmpty() -> return R.layout.subscribe_item
            data[position].title.length == 1 -> return R.layout.separator
            data[position].title.length == 2 -> return R.layout.copyright_item
        }
        return R.layout.item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data[position]
        if (getItemViewType(position) == R.layout.item) {
            when (model.font) {
                ModelMenuItem.HEADER_FONT -> {
                    holder.myTextView?.text = model.title.toUpperCase()
                    holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                            "fonts/IBMPlexSans-Bold.ttf")
                    holder.myTextView?.textSize = 15F
                }
                ModelMenuItem.BOTTOM_FONT -> {
                    holder.myTextView?.text = model.title
                    holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                            "fonts/IBMPlexSans-Text.ttf")
                    holder.myTextView?.textSize = 14F
                }
            }
        } else if (getItemViewType(position) == R.layout.copyright_item) {
            holder.myTextView?.typeface = Typeface.createFromAsset(holder.myTextView?.rootView?.context?.assets,
                    "fonts/IBMPlexSans-Text.ttf")
            holder.myTextView?.textSize = 12F
        }
    }

    override fun getItemCount() = data.size

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myTextView: TextView? = itemView.findViewById(R.id.tvTitle)
        val btnSubscribe: AppCompatButton? = itemView.findViewById(R.id.btnSubscribe)
    }
}