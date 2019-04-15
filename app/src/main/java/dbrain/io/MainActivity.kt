package dbrain.io

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.dbrain.facerecognizer.FaceRecognizer
import com.dbrain.textrecognizer.TextRecognizer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun launchText(v: View) {
        TextRecognizer.Builder(
            TextRecognizer.CAMERA_FACING_BACK,
            16f,
            10f,
            0.8f
        ).buildAndStart(this)
    }

    fun launchFace(v: View) {
        FaceRecognizer.Builder(
            FaceRecognizer.CAMERA_FACING_FRONT,
            true
        ).buildAndStart(this)
    }
}
