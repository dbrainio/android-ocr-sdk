package com.dbrain.recognition.processors

/**
 * Base data class for passing events from processor to listeners.
 * You can inherit from this class to pass your custom data.
 */
open class DataBundle(
    var detected: Boolean = false
)