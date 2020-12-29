package com.dbrain.flowdemo.adapters.holders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dbrain.flowdemo.R
import com.dbrain.flowdemo.adapters.Item

class PhotoPreviewHolder(parent: ViewGroup)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.photo_preview_holder, parent, false)) {
    private val photo = itemView.findViewById<ImageView>(R.id.image)

    fun bind(item: Item.PhotoPreview) {
        photo.setImageBitmap(item.image)
    }
}