package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;

public class FaceConsentActivity extends AppCompatActivity {

    private CheckBox cbConsent;
    private Button btnAccept;
    private Button btnSkip;
    private TextView tvPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FaceVault.hasConsent(this)) {
            startActivity(new Intent(this, FaceEnrollActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_face_consent);

        cbConsent = findViewById(R.id.cbConsent);
        btnAccept = findViewById(R.id.btnAccept);
        btnSkip = findViewById(R.id.btnSkip);
        tvPolicy = findViewById(R.id.tvPolicy);

        btnAccept.setEnabled(false);

        cbConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
        });

        btnAccept.setOnClickListener(v -> {
            if (!cbConsent.isChecked()) {
                Toast.makeText(this, "Debes aceptar el consentimiento para continuar", Toast.LENGTH_SHORT).show();
                return;
            }

            FaceVault.saveConsent(this, true);

            Intent intent = new Intent(FaceConsentActivity.this, FaceEnrollActivity.class);
            startActivity(intent);
            finish();
        });

        btnSkip.setOnClickListener(v -> {
            FaceVault.saveConsent(this, false);
            finish();
        });

        tvPolicy.setOnClickListener(v -> {
            openPolicy();
        });
    }

    private void openPolicy() {
        try {
            // Cambia esta URL por la real de tu política
            String policyUrl = "https://www.example.com/politica-datos";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la política de tratamiento de datos", Toast.LENGTH_SHORT).show();
        }
    }
}
