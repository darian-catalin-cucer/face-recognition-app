package brain_factory.face_recognition

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.nio.FloatBuffer

class MainActivity : AppCompatActivity()
{
    private val REQUEST_IMAGE_CAPTURE = 420

    private var addFaceButton: Button? = null
    private var authenticateButton: Button? = null
    private var rawBitmapImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        addFaceButton = findViewById<View>(R.id.AddFaceButton) as Button
        authenticateButton = findViewById<View>(R.id.AuthenticateButton) as Button

        authenticateButton!!.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            rawBitmapImage = intent!!.extras!!["data"] as Bitmap?
            val faceNet = FaceNet(assets)
            val embeddings: FloatBuffer = faceNet.getEmbeddings(rawBitmapImage!!, Rect(0, 0, 160, 160))
            Log.d("[embeddings]", embeddings[0].toString())
            faceNet.close()
        }
    }
}