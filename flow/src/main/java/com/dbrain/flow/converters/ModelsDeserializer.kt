package com.dbrain.flow.converters

import com.dbrain.flow.models.FlowRecognizeResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.json.JSONObject
import java.lang.reflect.Type


class ModelsDeserializer : JsonDeserializer<FlowRecognizeResponse?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlowRecognizeResponse? {
        json ?: return null
        val s = json.asString
        return try {
            FlowRecognizeResponse.parse(JSONObject(s))
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}