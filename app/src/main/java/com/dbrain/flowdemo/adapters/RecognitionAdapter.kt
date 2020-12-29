package com.dbrain.flowdemo.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dbrain.flow.models.FlowRecognizeItemField
import com.dbrain.flow.models.FlowRecognizeResponse
import com.dbrain.flowdemo.R
import com.dbrain.flowdemo.adapters.holders.HeaderHolder
import com.dbrain.flowdemo.adapters.holders.PhotoPreviewHolder
import com.dbrain.flowdemo.adapters.holders.ResponseItemHolder

class RecognitionAdapter(context: Context, photo: Bitmap, flowResponse: FlowRecognizeResponse) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = ArrayList<Item>()

    init {
        data.add(Item.PhotoPreview(photo))
        data.add(Item.Header(context.getString(R.string.fields_and_values), context.getString(R.string.accuracy)))
        flowResponse.items?.firstOrNull()?.fields?.forEach { data.add(Item.ResponseItem(it)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Item.createHolder(parent, viewType)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = data[position].bind(holder)
    override fun getItemViewType(position: Int) = data[position].id
    override fun getItemCount() = data.size
}

sealed class Item(val id: Int) {
    data class PhotoPreview(val image: Bitmap) : Item(PHOTO_PREVIEW)
    data class Header(val title: String, val text: String) : Item(HEADER)
    data class ResponseItem(val item: FlowRecognizeItemField) : Item(RESPONSE_ITEM)

    open fun bind(holder: RecyclerView.ViewHolder) {
        when (this) {
            is PhotoPreview -> (holder as? PhotoPreviewHolder)?.bind(this)
            is Header -> (holder as? HeaderHolder)?.bind(this)
            is ResponseItem -> (holder as? ResponseItemHolder)?.bind(this)
        }
    }

    companion object {
        const val PHOTO_PREVIEW = 0
        const val HEADER = 1
        const val RESPONSE_ITEM = 2

        fun createHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                PHOTO_PREVIEW -> PhotoPreviewHolder(parent)
                HEADER -> HeaderHolder(parent)
                RESPONSE_ITEM -> ResponseItemHolder(parent)
                else -> throw Exception("Invalid view type")
            }
        }
    }
}