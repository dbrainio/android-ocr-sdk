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
import com.dbrain.flow.common.ResponseHolder.responses
import com.dbrain.flow.flows.DocumentFlow
import com.dbrain.flow.flows.FlowType
import com.dbrain.flow.models.FlowClassifyResponse
import com.dbrain.flow.models.FlowRecognizeResponse
import com.dbrain.flow.models.Response
import com.dbrain.flow.workers.ClassifyWorker
import com.dbrain.flow.workers.RecognizeWorker
import com.dbrain.flow.workers.UploadWorker
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface FlowListener {
    /**
     * Called when photo has been taken after calling takePhoto().
     * @see Flow.takePhoto
     *
     * @param flow Instance of the current flow.
     * @param file Image file.
     **/
    fun onPhotoTaken(flow: Flow, file: File)

    /**
     * Called when image has been recognized by DBrain.
     * @see Flow.recognize
     *
     * @param flow Instance of the current flow.
     * @param result Recognition result.
     */
    fun onRecognized(flow: Flow, result: FlowRecognizeResponse)

    /**
     * Called when image has been classified by DBrain.
     * @see Flow.classify
     *
     * @param flow Instance of the current flow.
     * @param result Recognition result.
     */
    fun onClassified(flow: Flow, result: FlowClassifyResponse)

    /**
     * Called when some error occurred.
     * @param flow Instance of the current flow.
     * @param throwable Error throwable.
     */
    fun onError(flow: Flow, throwable: Throwable)

    /**
     * Called when loading status has been changed. Use it to update views such as ProgressBars.
     * @param status Current loading status
     */
    fun onStatusChanged(status: Status)
}

enum class Status {
    IDLE,
    LOADING,
    FINISHED
}

@SuppressLint("EnqueueWork")
class Flow(private val token: String, private val apiBaseUrl: String = DEFAULT_URL) : Observer<WorkInfo>, BroadcastReceiver() {
    private var workManager: WorkManager? = null
    private var appContext: Context? = null
        set(v) {
            if (field == null && v != null) {
                field = v
                if (workManager == null) {
                    workManager = WorkManager.getInstance(v)
                }
            }
        }
    private var broadcastReceiver: BroadcastReceiver? = null
    private var listener: FlowListener? = null
    private var workRequests = ConcurrentHashMap<UUID, Work>()
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

    /**
     * @return Requested document flow type
     */
    fun getType() = this.type

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
        LocalBroadcastManager.getInstance(context.applicationContext).registerReceiver(broadcastReceiver!!, IntentFilter(ACTION_FLOW))
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
        this.appContext = context.applicationContext

        val work = Work(
            validate = { result -> result is FlowRecognizeResponse && !result.items.isNullOrEmpty()},
            success = { result -> listener?.onRecognized(this, result as FlowRecognizeResponse) }
        )

        startWorker(file, RecognizeWorker::class.java, work)
    }

    /**
     * Classifies image file with DBrain. Result will be sent to onClassified method of the FlowListener.
     * Internet connection is required for this step.
     * @param context Activity context
     * @param file Image file
     */
    fun classify(context: Context, file: File) {
        this.appContext = context.applicationContext

        val work = Work(
            validate = { result -> result is FlowClassifyResponse && !result.items.isNullOrEmpty()},
            success = { result -> listener?.onClassified(this, result as FlowClassifyResponse) }
        )

        startWorker(file, ClassifyWorker::class.java, work)
    }

    private fun startWorker(file: File, worker: Class<out UploadWorker>, workData: Work) {
        val workRequest = OneTimeWorkRequest.Builder(worker)
            .setInputData(makeWorkData(file))
            .build()
        val workContinuation = workManager?.beginUniqueWork(UUID.randomUUID().toString(), ExistingWorkPolicy.REPLACE, workRequest) ?: return
        workContinuation.enqueue()

        workRequests[workRequest.id] = workData
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observeForever(this)
    }

    /**
     * Cancel recognition task.
     * @see Flow.recognize
     */
    fun cancel() {
        if (workManager == null) throw Exception("Called cancel without calling recognize() or classify()")
        workManager?.cancelAllWork()
        destroy()
    }

    private fun makeWorkData(file: File): Data =  workDataOf(
        ARG_URL to apiBaseUrl,
        ARG_FILE to file.absolutePath,
        ARG_TOKEN to token,
        ARG_FLOW_TYPE to Gson().toJson(type)
    )

    private fun destroy() {
        workRequests.forEach {
            workManager?.getWorkInfoByIdLiveData(it.key)?.removeObserver(this@Flow)
        }
        workRequests.clear()
        unregisterReceiver()
        workManager = null
    }

    private fun stopWork(id: UUID) {
        workManager?.getWorkInfoByIdLiveData(id)?.removeObserver(this@Flow)
        workRequests.remove(id)
    }

    private fun unregisterReceiver() {
        broadcastReceiver?.let { receiver ->
            appContext?.let { context ->
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
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
        workInfo ?: return

        val responseId = workInfo.outputData.getString(ARG_RESPONSE)
        val error = workInfo.outputData.getString(ARG_ERROR)

        val work = workRequests[workInfo.id]

        val flowResult = if (responseId != null) responses[responseId] else null

        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                changeStatus(Status.LOADING)
            }
            WorkInfo.State.SUCCEEDED -> {
                if (work?.validate?.invoke(flowResult) == true) work.success(flowResult)
                else listener?.onError(this, NoResultException())
            }
            WorkInfo.State.FAILED -> {
                if (error != null) {
                    listener?.onError(this, Exception(error))
                } else {
                    when (flowResult?.responseCode) {
                        ERROR_CODE_INVALID_TOKEN -> listener?.onError(this, InvalidTokenException())
                        else -> listener?.onError(this, NoResultException())
                    }
                }
            }
            else -> Unit
        }

        if (workInfo.state.isFinished) {
            changeStatus(Status.FINISHED)
            stopWork(workInfo.id)
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

internal class Work(
    val validate: (Response?) -> Boolean,
    val success: (Response?) -> Unit
)