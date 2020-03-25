package dbrain.io

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.dbrain.recognition.data.ClassifiedItem
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getColorCompat

class ClassifiedActivity : Activity() {

    private lateinit var titles: Array<String>
    private lateinit var spinnerView: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classified)
        val items = intent.getParcelableArrayListExtra<ClassifiedItem>(ITEMS)
        titles = ClassifiedItem.titles(items)
        spinnerView = findViewById(R.id.spinner)
        val spinnerAdapter = SpinnerAdapter()
        spinnerView.adapter = spinnerAdapter
        spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerAdapter.notifyDataSetChanged()
            }
        }
        spinnerView.setSelection(0)

        findViewById<View>(R.id.recognize).setOnClickListener {
            RecognizedActivity.go(
                this,
                items[spinnerView.selectedItemPosition].document.type,
                intent.getStringExtra(FILE_NAME)
            )
        }

        findViewById<View>(R.id.go_back).setOnClickListener {
            finish()
        }
    }

    private inner class SpinnerAdapter: ArrayAdapter<String>(this, R.layout.custom_item_selected, titles) {

        private val itemPadding = dp(16f, this@ClassifiedActivity).toInt()
        private val checkDrawable = getDrawable(R.drawable.ic_check)?.apply {
            val iconSize = dp(16f, this@ClassifiedActivity).toInt()
            setBounds(0, 0, iconSize, iconSize)
            setTint(getColorCompat(R.color.backgroundButton))
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            view.setPadding(itemPadding, 0, itemPadding, 0)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, parent)
        }

        private fun getCustomView(position: Int, parent: ViewGroup): View {
            val view = layoutInflater.inflate(R.layout.custom_dropdown_item, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = titles[position]
            if (spinnerView.selectedItemPosition == position) {
                textView.setCompoundDrawables(null, null, checkDrawable, null);
                textView.setTextColor(getColorCompat(R.color.backgroundButton))
            } else {
                textView.setCompoundDrawables(null, null, null, null)
                textView.setTextColor(getColorCompat(android.R.color.black))
            }
            return view
        }
    }

    companion object {
        private const val ITEMS = "items"
        private const val FILE_NAME = "file_name"

        fun go(context: Context, classifiedItems: ArrayList<ClassifiedItem>, fileName: String) {
            context.startActivity(Intent(context, ClassifiedActivity::class.java).apply{
                putParcelableArrayListExtra(ITEMS, classifiedItems)
                putExtra(FILE_NAME, fileName)
            })
        }
    }
}