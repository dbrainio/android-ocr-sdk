package com.dbrain.flow.converters

import com.dbrain.flow.models.FlowResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.json.JSONObject
import java.lang.reflect.Type


class ModelsDeserializer : JsonDeserializer<FlowResponse?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlowResponse? {
        json ?: return null
        val s = json.asString
        return try {
            FlowResponse.parse(JSONObject(s))
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}