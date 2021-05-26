package brain_factory.face_recognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

class AddNewPerson : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_person)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}