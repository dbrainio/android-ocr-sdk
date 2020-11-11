package com.dbrain.flow

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.FloatRange
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.dbrain.flow.activities.FlowActivity
import com.dbrain.flow.common.*
import com.dbrain.flow.common.ACTION_FLOW
import com.dbrain.flow.common.ARG_FLOW_PARAMS
import com.dbrain.flow.common.FlowParams
import com.dbrain.flow.common.WORK_UPLOAD_IMAGE
import com.dbrain.flow.flows.DocumentFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowResponse
import com.dbrain.flow.workers.UploadWorker
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.lang.Exception

interface FlowListener {
    /**
     * Called when photo has been taken after calling takePhoto().
     * @see Flow.takePhoto
     *
     * @param flow Instance of the current flow.
     * @param file Image file.
     **/
    fun onPhotoTaken(flow: Flow, file: File) = Unit

    /**
     * Called when image has been recognized by DBrain.
     * @see Flow.recognize
     *
     * @param flow Instance of the current flow.
     * @param result Recognition result.
     */
    fun onRecognized(flow: Flow, result: FlowResponse) = Unit

    /**
     * Called when some error occurred.
     * @param flow Instance of the current flow.
     * @param throwable Error throwable.
     */
    fun onError(flow: Flow, throwable: Throwable) = Unit

    /**
     * Called when loading status has been changed. Use it to update views such as ProgressBars.
     * @param status Current loading status
     */
    fun onStatusChanged(status: Status) = Unit
}

enum class Status {
    IDLE,
    LOADING,
    FINISHED
}

@SuppressLint("EnqueueWork")
class Flow(private val token: String) : Observer<WorkInfo>, BroadcastReceiver() {
    private lateinit var appContext: Context
    private var workManager: WorkManager? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var listener: FlowListener? = null
    private var workContinuation: WorkContinuation? = null
    private var workRequest: OneTimeWorkRequest? = null
    private var type: FlowType = DocumentFlow()
    private var clippingThreshold = 0.8f
    private var status = Status.IDLE

    /**
     * Specifies clipping threshold value from 0f to 1f. If the value equals 1f, then light will not be counted.
     * Otherwise shutter button is disabled if clipping was detected.
     * This value sets maximum height of the right edge of the histogram (max = maxValue * clippingThreshold).
     * @param value Max allowed clipping value
     */
    fun setClippingThreshold(@FloatRange(from = 0.0, to = 1.0) value: Float) {
        this.clippingThreshold = value
    }

    /**
     * Specifies document flow type
     * @param type Requested document flow
     */
    fun setType(type: FlowType) = apply {
        this.type = type
    }

    fun setListener(listener: FlowListener) = apply {
        this.listener = listener
        listener.onStatusChanged(status)
    }

    /**
     * Takes picture with camera. Result will be sent to onPhotoTaken method of the FlowListener
     * @param context Activity context
     */
    fun takePhoto(context: Context) {
        this.appContext = context.applicationContext
        broadcastReceiver = this
        LocalBroadcastManager.getInstance(appContext).registerReceiver(broadcastReceiver!!, IntentFilter(ACTION_FLOW))
        val intent = Intent(context, FlowActivity::class.java).apply {
            val aspect = this@Flow.type.bordersAspectRatio
            val params = FlowParams(
                aspect.width.toFloat(),
                aspect.height.toFloat(),
                this@Flow.type.bordersSizeMultiplier,
                clippingThreshold
            )
            putExtra(ARG_FLOW_PARAMS, params)
        }
        context.startActivity(intent)
    }

    /**
     * Recognizes image file with DBrain. Result will be sent to onRecognized method of the FlowListener.
     * Internet connection is required for this step.
     * @param context Activity context
     * @param file Image file
     */
    fun recognize(context: Context, file: File) {
        workManager = WorkManager.getInstance(context.applicationContext)

        val builder = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf(
                ARG_FILE to file.absolutePath,
                ARG_TOKEN to token,
                ARG_FLOW_TYPE to Gson().toJson(type)
            ))

        val workRequest = builder.build()
        this.workRequest = workRequest

        workContinuation = workManager?.beginUniqueWork(WORK_UPLOAD_IMAGE, ExistingWorkPolicy.REPLACE, workRequest)
        workContinuation?.enqueue()
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observeForever(this)
    }

    /**
     * Cancel recognition task.
     * @see Flow.recognize
     */
    fun cancel() {
        if (workManager == null) throw Exception("Called cancel without calling recognize()")
        workManager?.cancelAllWork()
        destroy()
    }

    private fun destroy() {
        stopWork()
        unregisterReceiver()
    }

    private fun stopWork() {
        workRequest?.let {
            workManager?.getWorkInfoByIdLiveData(it.id)?.removeObserver(this@Flow)
            workRequest = null
            workManager = null
        }
    }

    private fun unregisterReceiver() {
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(it)
            broadcastReceiver = null
        }
    }

    private fun changeStatus(status: Status) {
        if (this.status != status) {
            this.status = status
            listener?.onStatusChanged(status)
        }
    }

    override fun onChanged(workInfo: WorkInfo?) {
        val response = workInfo?.outputData?.getString(ARG_RESPONSE)
        val error = workInfo?.outputData?.getString(ARG_ERROR)

        val flowResult = response?.run { FlowResponse.parse(JSONObject(this)) }

        when (workInfo?.state) {
            WorkInfo.State.RUNNING -> {
                changeStatus(Status.LOADING)
            }
            WorkInfo.State.SUCCEEDED -> {
                if (flowResult != null) {
                    if (!flowResult.items.isNullOrEmpty()) {
                        listener?.onRecognized(this, flowResult)
                    } else {
                        listener?.onError(this, NoResultException(flowResult))
                    }
                } else {
                    listener?.onError(this, NoResultException(flowResult))
                }
            }
            WorkInfo.State.FAILED -> {
                listener?.onError(this, if (error != null) Exception(error) else NoResultException(flowResult))
            }
            else -> {

            }
        }

        if (workInfo?.state?.isFinished == true) {
            changeStatus(Status.FINISHED)
            stopWork()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.getStringExtra(ACTION_FLOW_ACTION)) {
            ACTION_FLOW_PHOTO_CAPTURED -> {
                listener?.onPhotoTaken(this, intent.getSerializableExtra(ARG_FILE) as? File ?: return)
                unregisterReceiver()
            }
            ACTION_FLOW_PHOTO_CAPTURE_CANCELLED -> {
                unregisterReceiver()
            }
        }
    }

}