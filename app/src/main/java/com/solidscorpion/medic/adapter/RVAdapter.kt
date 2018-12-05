package com.solidscorpion.medic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solidscorpion.medic.R
import com.solidscorpion.medic.pojo.ModelMenuItem


class RVAdapter
internal constructor(context: Context, private val mData: ArrayList<ModelMenuItem>?) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mData!![position]
        holder.myTextView.text = model.title
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var myTextView: TextView = itemView.findViewById(R.id.tvTitle)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(mData!![adapterPosition])
        }
    }

    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(modelMenuItem: ModelMenuItem)
    }
}