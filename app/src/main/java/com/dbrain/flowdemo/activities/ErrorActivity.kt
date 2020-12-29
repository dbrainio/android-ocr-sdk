package com.dbrain.flowdemo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dbrain.flow.common.InvalidTokenException
import com.dbrain.flow.common.NoResultException
import com.dbrain.flowdemo.R
import kotlinx.android.synthetic.main.activity_error.*

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val throwable = intent?.extras?.get(ARG_THROWABLE) as? Throwable
        if (throwable == null) {
            finish()
            return
        }

        val (errTitle, errText) = when (throwable) {
            is NoResultException ->  R.string.cannot_recognize to R.string.cannot_recognize_descr
            is InvalidTokenException -> R.string.invalid_token to R.string.cannot_recognize_descr
            else -> R.string.unknown_error to R.string.cannot_recognize_descr
        }

        title_text_view?.setText(errTitle)
        descr_text_view?.setText(errText)

        button?.setOnClickListener {
            finish()
        }
    }

    companion object {
        private const val ARG_THROWABLE = "throwable"
        fun show(context: Activity, throwable: Throwable) {
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra(ARG_THROWABLE, throwable)
            context.startActivity(intent)
        }
    }
}