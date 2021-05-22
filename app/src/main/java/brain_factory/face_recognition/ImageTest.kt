package brain_factory.face_recognition

import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class ImageTest : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_test)

        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val imagePath = intent.getStringExtra("image_test")

        val testImage = findViewById<ImageView>(R.id.test_image)
        testImage.setImageBitmap(ImageUtils.handleSamplingAndRotationBitmap(this, imagePath))
    }
}