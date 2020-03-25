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
import dbrain.io.cell.BackButtonCell
import dbrain.io.cell.RecognizedCell
import dbrain.io.cell.RecognizedTitleCell
import java.io.File
import java.lang.Exception

class RecognizedActivity : Activity(), Request.Callback<ArrayList<RecognizedItem>> {

    private lateinit var list: RecyclerView
    private lateinit var recognizeRequest: RecognizeRequest
    private var recognizedItems: ArrayList<RecognizedItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognized)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        recognizeRequest = RecognizeRequest(intent.getStringExtra(TYPE), File(intent.getStringExtra(FILE_NAME)))
        savedInstanceState?.getParcelableArrayList<RecognizedItem>(RECOGNIZED_ITEMS)?.let {
            recognizedItems = it
        }
        if (recognizedItems.size == 0) {
            recognizeRequest.responseUI(this)
        } else {
            initFields()
        }
    }

    private fun initFields() {
        list.visibility = View.VISIBLE
        list.adapter = Adapter(recognizedItems[0].fields)
        findViewById<View>(R.id.progress).visibility = View.GONE
    }

    override fun onResponse(response: ArrayList<RecognizedItem>) {
        recognizedItems = response
        if (recognizedItems.size == 0) {
            finish()
        } else {
            initFields()
        }
    }

    override fun onError(exception: Exception) {
        super.onError(exception)
        findViewById<View>(R.id.progress).visibility = View.GONE
        findViewById<View>(R.id.errorText).visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (recognizedItems.size > 0) {
            outState.putParcelableArrayList(RECOGNIZED_ITEMS, recognizedItems)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizeRequest.cancel()
    }

    private class Adapter(val fields: List<RecognizedField>): RecyclerView.Adapter<Holder>() {

        override fun onBindViewHolder(holder: Holder, position: Int) {
            if (holder.itemView is RecognizedCell) {
                holder.itemView.bind(fields[position - 1])
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val cell=  when(viewType) {
                VIEW_TYPE_TITLES -> RecognizedTitleCell(parent.context)
                VIEW_TYPE_BACK_BUTTON -> BackButtonCell(parent.context)
                else -> RecognizedCell(parent.context)
            }
            cell.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            return Holder(cell)
        }

        override fun getItemCount() = fields.size + 2

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                return VIEW_TYPE_TITLES
            } else if (position == itemCount - 1) {
                return VIEW_TYPE_BACK_BUTTON
            }
            return VIEW_TYPE_FIELD
        }
    }

    private class Holder(cellView: View): RecyclerView.ViewHolder(cellView)

    companion object {
        private const val VIEW_TYPE_FIELD = 1
        private const val VIEW_TYPE_TITLES = 2
        private const val VIEW_TYPE_BACK_BUTTON = 3

        private const val TYPE = "TYPE"
        private const val FILE_NAME = "file_name"
        private const val RECOGNIZED_ITEMS = "recognized_items"

        fun go(context: Context, type: String, fileName: String) {
            context.startActivity(Intent(context, RecognizedActivity::class.java).apply{
                putExtra(TYPE, type)
                putExtra(FILE_NAME, fileName)
            })
        }
    }
}