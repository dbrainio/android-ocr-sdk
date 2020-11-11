package com.dbrain.flow.models

import org.json.JSONObject

data class FlowResponse(
    val detail: List<FlowDetail>?,
    val items: List<FlowItem>?,
    val taskId: String?,
    val code: Int,
    val message: String?,
    val errno: Int,
    val traceback: String?,
    val fake: Boolean?,
    val pagesCount: Int,
    val docsCount: Int,
    val raw: String?
) {
    internal companion object {
        fun parse(json: JSONObject) : FlowResponse {
            val details = json.optJSONArray("detail")?.run {
                val list = ArrayList<FlowDetail>()
                for (i in 0 until length()) {
                    list.add(FlowDetail.parse(optJSONObject(i)))
                }
                list
            }
            val items = json.optJSONArray("items")?.run {
                val list = ArrayList<FlowItem>()
                for (i in 0 until length()) {
                    list.add(FlowItem.parse(optJSONObject(i)))
                }
                list
            }
            return FlowResponse(
                details,
                items,
                json.optString("task_id"),
                json.optInt("code"),
                json.optString("message"),
                json.optInt("errno"),
                json.optString("traceback"),
                json.optBoolean("fake"),
                json.optInt("pages_count"),
                json.optInt("docs_count"),
                json.toString()
            )
        }
    }
}

data class FlowDetail(
    val loc: List<String>?,
    val msg: String,
    val type: String
) {
    internal companion object {
        fun parse(json: JSONObject) : FlowDetail {
            val loc = json.optJSONArray("loc")?.run {
                val list = ArrayList<String>()
                for (i in 0 until length()) {
                    list.add(optString(i))
                }
                list
            }
            return FlowDetail(
                loc,
                json.optString("msg"),
                json.optString("type")
            )
        }
    }
}

data class FlowItem(
    val docType: String,
    val fields: List<FlowItemField>,
    val color: Boolean,
    val error: String?
) {
    internal companion object {
        fun parse(json: JSONObject) : FlowItem {
            val fields = ArrayList<FlowItemField>()
            json.optJSONObject("fields")?.let { jFields ->
                jFields.keys().forEach {
                    val jField = jFields.optJSONObject(it)
                    fields.add(FlowItemField.parse(it, jField))
                }
            }
            return FlowItem(
                json.optString("doc_type"),
                fields,
                json.optBoolean("color"),
                json.optString("error")
            )
        }
    }
}

data class FlowItemField(
    val name: String,
    val text: String,
    val confidence: Double?,
    val valid: Boolean?,
    val coords: List<Pair<Double, Double>>?
) {
    internal companion object {
        fun parse(name: String, json: JSONObject) : FlowItemField {
            val coords = json.optJSONArray("coords")?.run {
                val list = ArrayList<Pair<Double, Double>>()
                for (i in 0 until length()) {
                    val arr = optJSONArray(i)
                    list.add(arr.optDouble(0) to arr.optDouble(1))
                }
                list
            }
            return FlowItemField(
                name,
                json.optString("text"),
                json.optDouble("confidence"),
                if (!json.isNull("valid")) json.optBoolean("valid") else null,
                coords
            )
        }
    }
}