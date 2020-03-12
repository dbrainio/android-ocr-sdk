package com.dbrain.recognition.api

import com.dbrain.recognition.api.internal.Request
import com.dbrain.recognition.data.ClassifiedItem
import org.json.JSONObject
import java.io.File

class ClassifyRequest(file: File): Request<ArrayList<ClassifiedItem>>("classify") {

    init {
        setDocumentImage(file)
    }

    override fun parseResponse(json: JSONObject): ArrayList<ClassifiedItem> {
        return ClassifiedItem.list(json.optJSONArray(Key.ITEMS))
    }
}