package com.marcos.fisikappmovil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.security.CredentialVault;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.ui.Autenticacion.Login;
import com.marcos.fisikappmovil.ui.faceNet.FaceVerifyActivity;

public class SplashAuthActivity extends AppCompatActivity {

    private VideoView videoView;
    private ImageView staticSplash;

    private TokenManager tokenManager;

    private boolean alreadyNavigated = false;
    private boolean videoInterrupted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_FisikappMovil);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_auth);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        tokenManager = new TokenManager(this);

        initViews();
        startIntroVideo();
    }

    private void initViews() {
        videoView = findViewById(R.id.videoIntro);
        staticSplash = findViewById(R.id.staticSplash);

        staticSplash.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
    }

    private void startIntroVideo() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.screen_company);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> videoView.start());

        videoView.setOnCompletionListener(mp -> resolveNextScreen());

        videoView.setOnErrorListener((mp, what, extra) -> {
            showStaticSplash();
            resolveNextScreen();
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        videoInterrupted = true;

        if (videoView != null) {
            videoView.stopPlayback();
            videoView.setVisibility(View.GONE);
        }

        showStaticSplash();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoInterrupted && !alreadyNavigated) {
            showStaticSplash();
            resolveNextScreen();
        }
    }

    private void showStaticSplash() {
        if (staticSplash != null) {
            staticSplash.setVisibility(View.VISIBLE);
        }

        if (videoView != null) {
            videoView.setVisibility(View.GONE);
        }
    }

    private void resolveNextScreen() {
        if (alreadyNavigated) {
            return;
        }

        alreadyNavigated = true;

        if (tokenManager.hasValidAccessToken()) {
            String rol = tokenManager.getUserRole();

            if (rol == null || !rol.trim().equalsIgnoreCase("estudiante")) {
                tokenManager.clearSession();
                goToLogin();
                return;
            }

            goToDashboard();
            return;
        }

        if (canUseFaceLogin()) {
            goToFaceVerify();
            return;
        }

        goToLogin();
    }

    private boolean canUseFaceLogin() {
        return FaceVault.hasConsent(this)
                && FaceVault.hasEmbedding(this)
                && CredentialVault.hasCredentials(this);
    }

    private void goToDashboard() {
        Intent intent = new Intent(SplashAuthActivity.this, Dashboard.class);
        startActivity(intent);
        finish();
    }

    private void goToFaceVerify() {
        Intent intent = new Intent(SplashAuthActivity.this, FaceVerifyActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(SplashAuthActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}