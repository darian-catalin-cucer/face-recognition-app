package brain_factory.face_recognition

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import java.io.File
import java.nio.FloatBuffer
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity()
{
    private val REQUEST_IMAGE_CAPTURE = 420

    private lateinit var addFaceButton: Button
    private lateinit var authenticateButton: Button

    private lateinit var currentPhotoPath: String

    private lateinit var backgroundThreadRealm: Realm
    private lateinit var realmApp: App

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Realm.init(this)
        realmApp = App(AppConfiguration.Builder("facerecognition-awxuy").build())

        val apiKeyCredentials = Credentials.apiKey("eEvFzGCK1lRXLs0w2jq3u1JaJtG91dS1TF1FYGK5Vg9Bq9f94FOySHyksKO2fkhL")
        var user: User? = null
        realmApp.loginAsync(apiKeyCredentials) {
            if (it.isSuccess)
            {
                Log.v("[AUTH]", "Successfully authenticated using an API Key.")
                user = realmApp.currentUser()
                val config = SyncConfiguration.Builder(realmApp.currentUser(), "FaceRecognition")
                    .allowQueriesOnUiThread(true).allowWritesOnUiThread(true).build()
                backgroundThreadRealm = Realm.getInstance(config)
            }
            else
            {
                Log.e("[AUTH]", "Error logging in: ${it.error}")
            }
        }


        addFaceButton = findViewById<View>(R.id.AddFaceButton) as Button
        authenticateButton = findViewById<View>(R.id.AuthenticateButton) as Button

        authenticateButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        addFaceButton.setOnClickListener {
            startActivity(Intent(this, AddNewPersonActivity::class.java));
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        backgroundThreadRealm.close()
        realmApp.currentUser()?.logOutAsync() {
            if (it.isSuccess)
            {
                Log.v("[AUTH]", "Successfully logged out.")
            }
            else
            {
                Log.e("[AUTH]", "Failed to log out, error: ${it.error}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            val embeddings: FloatBuffer = ImageProcess.getEmbeddings(currentPhotoPath, assets)
            val person = Person("Cristian", "Cristea", "cristiancristea00@gmail.com", "+40770779947", "Admin", embeddings)
            backgroundThreadRealm.executeTransaction { realm -> realm.insert(person) }
            Log.d("[embeddings]", embeddings[0].toString() + " " + embeddings[1].toString() + " " + embeddings[2].toString()) // DEBUG
            val displayIntent = Intent(this, ImageTestActivity::class.java).apply {
                putExtra("image_test", currentPhotoPath)
            }
            startActivity(displayIntent)
        }
    }

    private fun createImageFile(): File
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("${timeStamp}_", ".jpg", storageDir).apply { currentPhotoPath = absolutePath }
    }

    private fun dispatchTakePictureIntent()
    {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File = createImageFile()
                photoFile.also {
                    val photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
}