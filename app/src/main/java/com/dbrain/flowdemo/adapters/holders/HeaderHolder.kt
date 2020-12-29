package com.dbrain.flowdemo.adapters.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dbrain.flowdemo.R
import com.dbrain.flowdemo.adapters.Item

class HeaderHolder(parent: ViewGroup)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.header_holder, parent, false)) {
    private val title = itemView.findViewById<TextView>(R.id.title)
    private val text = itemView.findViewById<TextView>(R.id.text)

    fun bind(item: Item.Header) {
        title.text = item.title
        text.text = item.text
    }
}