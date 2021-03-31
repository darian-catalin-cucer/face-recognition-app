package brain_factory.face_recognition;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    Button AddFaceButton;
    Button AuthenticateButton;

    TextView DebugMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AuthenticateButton = findViewById(R.id.AuthenticateButton);
        AddFaceButton = findViewById(R.id.AddFaceButton);

        DebugMessage = findViewById(R.id.DebugMessage);
        DebugMessage.setVisibility(View.GONE);

        AuthenticateButton.setOnClickListener((view) ->
        {
            DebugMessage.setVisibility(View.VISIBLE);
            DebugMessage.setText("The AuthenticateButton button was clicked");
            DebugMessage.postDelayed(() -> DebugMessage.setVisibility(View.GONE), 1500);
        });

        AddFaceButton.setOnClickListener((view) ->
        {
            DebugMessage.setVisibility(View.VISIBLE);
            DebugMessage.setText("The AddFaceButton button was clicked");
            DebugMessage.postDelayed(() -> DebugMessage.setVisibility(View.GONE), 1500);
        });
    }
}