package com.dbrain.flow.common

import com.dbrain.flow.models.Response
import java.util.concurrent.ConcurrentHashMap

internal object ResponseHolder {
    val responses = ConcurrentHashMap<String, Response>()
}