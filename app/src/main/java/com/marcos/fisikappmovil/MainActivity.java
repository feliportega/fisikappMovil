package com.marcos.fisikappmovil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private int lastPosition = 0;

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Si el video no ha terminado, continuar flujo
        goToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.seekTo(lastPosition);
            videoView.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_FisikappMovil);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        videoView = findViewById(R.id.videoIntro);

        // Ruta del video en /res/raw
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.screen_company);
        videoView.setVideoURI(uri);

        videoView.start();

        // Cuando termine el video → ir a Login
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}
