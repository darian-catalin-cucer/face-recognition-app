package brain_factory.face_recognition

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.SyncConfiguration
import java.io.File
import java.nio.FloatBuffer
import java.text.SimpleDateFormat
import java.util.*

class AddNewPersonActivity : AppCompatActivity()
{
    private val REQUEST_IMAGE_CAPTURE = 420

    private lateinit var scanFaceButton: Button
    private lateinit var addPersonButton: Button

    private lateinit var firstNameField: TextView
    private lateinit var lastNameField: TextView
    private lateinit var emailField: TextView
    private lateinit var phoneField: TextView
    private lateinit var positionField: TextView

    private lateinit var currentPhotoPath: String

    private lateinit var embeddings: FloatBuffer

    private lateinit var realmApp: App
    private lateinit var backgroundThreadRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_person)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        scanFaceButton = findViewById(R.id.scanFaceButton)
        addPersonButton = findViewById(R.id.addPersonButton)

        firstNameField = findViewById(R.id.personFirstName)
        lastNameField = findViewById(R.id.personLastName)
        emailField = findViewById(R.id.personEmail)
        phoneField = findViewById(R.id.personPhone)
        positionField = findViewById(R.id.personPosition)

        scanFaceButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        addPersonButton.setOnClickListener {
            dispatchAddPersonInDatabase()
        }

        Realm.init(this)
        realmApp = App(AppConfiguration.Builder("facerecognition-awxuy").build())

        val apiKeyCredentials = Credentials.apiKey("eEvFzGCK1lRXLs0w2jq3u1JaJtG91dS1TF1FYGK5Vg9Bq9f94FOySHyksKO2fkhL")
        realmApp.loginAsync(apiKeyCredentials) {
            if (it.isSuccess)
            {
                Log.v("[AUTH]", "Successfully authenticated using an API Key.")
                val config = SyncConfiguration.Builder(realmApp.currentUser(), "FaceRecognition")
                    .allowQueriesOnUiThread(true).allowWritesOnUiThread(true).build()
                backgroundThreadRealm = Realm.getInstance(config)
            }
            else
            {
                Log.e("[AUTH]", "Error logging in: ${it.error}")
            }
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
            embeddings = ImageProcess.getEmbeddings(currentPhotoPath, assets)
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

    private fun dispatchAddPersonInDatabase()
    {
        val person = Person(
            firstNameField.text.toString(), lastNameField.text.toString(), emailField.text.toString(), phoneField.text.toString(),
            positionField.text.toString(), embeddings
        )
        backgroundThreadRealm.executeTransaction { realm -> realm.insert(person) }
    }
}