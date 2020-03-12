package com.dbrain.recognition.api.internal

import java.io.IOException

class APIException(override val message: String) : IOException(message)