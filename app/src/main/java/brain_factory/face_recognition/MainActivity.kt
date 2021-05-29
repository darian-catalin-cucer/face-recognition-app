package brain_factory.face_recognition

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Button
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


class MainActivity : AppCompatActivity()
{
    private val REQUEST_IMAGE_CAPTURE = 420

    private lateinit var addFaceButton: Button
    private lateinit var authenticateButton: Button

    private lateinit var currentPhotoPath: String

    private lateinit var realmApp: App
    private lateinit var realmConfig: SyncConfiguration
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        addFaceButton = findViewById(R.id.AddFaceButton)
        authenticateButton = findViewById(R.id.AuthenticateButton)

        authenticateButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        addFaceButton.setOnClickListener {
            startActivity(Intent(this, AddNewPersonActivity::class.java));
        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Realm.init(this)
        realmApp = App(AppConfiguration.Builder("facerecognition-awxuy").build())
        val anonymousCredentials = Credentials.anonymous()
        realmApp.login(anonymousCredentials)
        realmConfig = SyncConfiguration.Builder(realmApp.currentUser(), "FaceRecognition")
            .allowQueriesOnUiThread(true).allowWritesOnUiThread(true).build()
        realm = Realm.getInstance(realmConfig)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        realmApp.currentUser()?.logOut()
        realm.close()
    }

    override fun onResume()
    {
        super.onResume()
        val anonymousCredentials = Credentials.anonymous()
        realmApp.login(anonymousCredentials)
    }

    override fun onPause()
    {
        super.onPause()
        realmApp.currentUser()?.logOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            val embeddings: FloatBuffer = ImageProcess.getEmbeddings(currentPhotoPath, assets)
            val finding = dispatchQueryPerson(embeddings)
            if (finding.first)
            {
                Log.d("[PERSON]", finding.second.toString())
            }
            else
            {
                Log.d("[PERSON]", "Person not found in database")
            }
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

    private fun dispatchQueryPerson(embeddings: FloatBuffer): Pair<Boolean, Person>
    {
        val persons = realm.where(Person::class.java).findAll().createSnapshot()
        var result: Pair<Boolean, Person>? = null

        var minDistance = Double.MAX_VALUE
        var currentDistance: Double

        for (person in persons)
        {
            currentDistance = Utils.computeEuclideanDistance(embeddings, person.embeddings)
            if (currentDistance < minDistance)
            {
                minDistance = currentDistance
                result = Pair(true, person)
                person.toString()
            }
        }
        return result ?: Pair(false, Person())
    }

}