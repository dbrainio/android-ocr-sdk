package dbrain.io

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dbrain.facerecognizer.FaceRecognizer
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.textrecognizer.TextRecognizer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun launchText(v: View) {
        TextRecognizer.Builder(
            TextRecognizer.CAMERA_FACING_BACK,
            3.5f,
            4.9f,
            0.5f
        ).buildAndStartForResult(this, TEXT_RECOGNIZER_RESULT)
    }

    fun launchFace(v: View) {
        FaceRecognizer.Builder(
            FaceRecognizer.CAMERA_FACING_FRONT,
            true
        ).buildAndStart(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEXT_RECOGNIZER_RESULT && data != null) {
            val isSuccessfully = data.getBooleanExtra(CaptureActivity.ARG_SUCCESSFULLY, false)
            if (isSuccessfully) {
                ClassifiedActivity.go(this, data.getParcelableArrayListExtra(CaptureActivity.ARG_CLASSIFIED_LIST), data.getStringExtra(CaptureActivity.ARG_FILE))
            }
        }
    }

    companion object {
        private const val TEXT_RECOGNIZER_RESULT = 1
    }
}
