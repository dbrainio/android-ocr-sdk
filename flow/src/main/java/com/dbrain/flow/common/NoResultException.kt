package com.dbrain.flow.common

import com.dbrain.flow.models.FlowResponse
import java.lang.Exception

class NoResultException(val response: FlowResponse?):Exception("No result")