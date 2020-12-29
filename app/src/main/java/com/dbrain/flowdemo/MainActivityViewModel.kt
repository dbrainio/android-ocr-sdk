package com.dbrain.flowdemo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dbrain.flow.Flow
import com.dbrain.flow.FlowListener
import com.dbrain.flow.Status
import com.dbrain.flow.flows.DocumentFlow
import com.dbrain.flow.flows.DriverLicenceFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.flows.PassportFlow
import com.dbrain.flow.models.FlowClassifyResponse
import com.dbrain.flow.models.FlowRecognizeResponse
import java.io.File


class MainActivityViewModel(application: Application) : AndroidViewModel(application), FlowListener {
    val loadingStatus: MutableLiveData<Status> by lazy { MutableLiveData() }
    val events: MutableLiveData<Event> by lazy { MutableLiveData() }
    private val flow = Flow(TODO("Insert token here")).setListener(this)

    private var photo: File? = null
    private var recognizeResult: FlowRecognizeResponse? = null
    private var classifyResult: FlowClassifyResponse? = null

    init {
        setFlowType(PassportFlow())
    }

    fun setFlowType(type: FlowType) {
        flow.setType(type)
    }

    fun setPhotoFile(file: File) {
        this.photo = file
    }

    fun takePicture(context: Context, clippingThreshold: Float = 0.8f) {
        flow.setClippingThreshold(clippingThreshold)
        flow.takePhoto(context)
    }

    fun recognize(context: Context) {
        flow.recognize(context, photo ?: return)
    }

    fun classify(context: Context) {
        flow.classify(context, photo ?: return)
    }

    fun getFlowType() = flow.getType()
    fun getPhotoFile() = photo

    override fun onPhotoTaken(flow: Flow, file: File) {
        this.photo = file
        events.postValue(PhotoTakenEvent(file))
    }

    override fun onRecognized(flow: Flow, result: FlowRecognizeResponse) {
        this.recognizeResult = result
        events.postValue(RecognizedEvent(result))
    }

    override fun onClassified(flow: Flow, result: FlowClassifyResponse) {
        if (result.items.isNullOrEmpty()) return
        this.classifyResult = result

        val newFlowType = when {
            PassportFlow.DOC_TYPES.contains(result.items?.get(0)?.document?.type) -> PassportFlow()
            DriverLicenceFlow.DOC_TYPES.contains(result.items?.get(0)?.document?.type) -> DriverLicenceFlow()
            else -> DocumentFlow()
        }
        setFlowType(newFlowType)
        events.postValue(ClassifiedEvent(result))
    }

    override fun onError(flow: Flow, throwable: Throwable) {
        events.postValue(ErrorEvent(throwable))
    }

    override fun onStatusChanged(status: Status) {
        loadingStatus.postValue(status)
    }
}

open class Event {
    var handled = false

    fun handle(action: () -> Unit) {
        if (handled) return
        handled = true
        action()
    }
}

data class PhotoTakenEvent(val file: File) : Event()
data class RecognizedEvent(val result: FlowRecognizeResponse) : Event()
data class ClassifiedEvent(val result: FlowClassifyResponse) : Event()
data class ErrorEvent(val throwable: Throwable) : Event()