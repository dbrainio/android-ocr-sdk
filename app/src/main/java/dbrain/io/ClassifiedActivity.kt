package dbrain.io

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.dbrain.recognition.data.ClassifiedItem

class ClassifiedActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classified)
        val items = intent.getParcelableArrayListExtra<ClassifiedItem>(ITEMS)
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ClassifiedItem.titles(items))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        findViewById<View>(R.id.recognize).setOnClickListener {
            RecognizedActivity.go(
                this,
                items[spinner.selectedItemPosition].document.type,
                intent.getStringExtra(FILE_NAME)
            )
        }

        findViewById<View>(R.id.go_back).setOnClickListener {
            finish()
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