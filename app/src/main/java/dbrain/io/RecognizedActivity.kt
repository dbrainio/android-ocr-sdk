package dbrain.io

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dbrain.recognition.api.RecognizeRequest
import com.dbrain.recognition.api.internal.Request
import com.dbrain.recognition.data.RecognizedField
import com.dbrain.recognition.data.RecognizedItem
import dbrain.io.cell.RecognizedCell
import java.io.File
import java.lang.Exception

class RecognizedActivity : Activity(), Request.Callback<ArrayList<RecognizedItem>> {

    private lateinit var list: RecyclerView
    private lateinit var recognizeRequest: RecognizeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognized)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        recognizeRequest = RecognizeRequest(intent.getStringExtra(TYPE), File(intent.getStringExtra(FILE_NAME)))
        recognizeRequest.responseUI(this)
    }

    override fun onResponse(response: ArrayList<RecognizedItem>) {
        list.visibility = View.VISIBLE
        list.adapter = Adapter(response[0].fields)
        findViewById<View>(R.id.progress).visibility = View.GONE
    }

    override fun onError(exception: Exception) {
        super.onError(exception)
        findViewById<View>(R.id.progress).visibility = View.GONE
        findViewById<View>(R.id.errorText).visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizeRequest.cancel()
    }

    private class Adapter(val fields: List<RecognizedField>): RecyclerView.Adapter<Holder>() {

        override fun onBindViewHolder(holder: Holder, position: Int) {
            (holder.itemView as RecognizedCell).bind(fields[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val cell = RecognizedCell(parent.context)
            cell.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            return Holder(cell)
        }

        override fun getItemCount() = fields.size
    }

    private class Holder(cellView: View): RecyclerView.ViewHolder(cellView)

    companion object {
        private const val TYPE = "TYPE"
        private const val FILE_NAME = "file_name"

        fun go(context: Context, type: String, fileName: String) {
            context.startActivity(Intent(context, RecognizedActivity::class.java).apply{
                putExtra(TYPE, type)
                putExtra(FILE_NAME, fileName)
            })
        }
    }
}