package com.dbrain.recognition.api

import com.dbrain.recognition.api.internal.Request
import com.dbrain.recognition.data.RecognizedItem
import org.json.JSONObject
import java.io.File

class RecognizeRequest(type: String, file: File): Request<ArrayList<RecognizedItem>>("recognize") {

    init {
        addQuery(Key.DOC_TYPE, type)
        setDocumentImage(file)
    }

    override fun parseResponse(json: JSONObject): ArrayList<RecognizedItem> {
        return RecognizedItem.list(json.optJSONArray(Key.ITEMS))
    }

}