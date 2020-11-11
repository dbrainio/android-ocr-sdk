
package com.dbrain.flowdemo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dbrain.flow.Flow
import com.dbrain.flow.FlowListener
import com.dbrain.flow.Status
import com.dbrain.flow.flows.DocumentFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowResponse
import java.io.File

class MainActivityViewModel(application: Application) : AndroidViewModel(application), FlowListener {
    internal val loadingStatus = MutableLiveData<Status>()
    internal val state: MutableLiveData<ModelState> by lazy {
        MutableLiveData(ModelState())
    }
    private val flow = Flow("").setListener(this) //TODO insert your key here

    fun takePicture(context: Context, type: FlowType = DocumentFlow(), clippingThreshold: Float = 0.8f) {
        flow.setType(type)
        flow.setClippingThreshold(clippingThreshold)
        flow.takePhoto(context)
    }

    fun recognize(context: Context, file: File) {
        flow.recognize(context, file)
    }

    override fun onPhotoTaken(flow: Flow, file: File) {
        state.value = ModelState(photoFile = file)
    }

    override fun onRecognized(flow: Flow, result: FlowResponse) {
        val value = state.value ?: ModelState()
        state.value = value.copy(result = result)
    }

    override fun onError(flow: Flow, throwable: Throwable) {
        state.value = ModelState(error = throwable)
    }

    override fun onStatusChanged(status: Status) {
        loadingStatus.value = status
    }
}

data class ModelState(
    val photoFile: File? = null,
    val result: FlowResponse? = null,
    val error: Throwable? = null
)
