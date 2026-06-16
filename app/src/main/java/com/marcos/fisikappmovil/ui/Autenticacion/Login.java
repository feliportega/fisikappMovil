package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.RecuperacionDeCuenta.RecuperarCuenta;
import com.marcos.fisikappmovil.ui.UnityAR.UnityArActivity;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;
import com.marcos.fisikappmovil.ui.faceNet.FaceVerifyActivity;

public class Login extends AppCompatActivity {
    Button btnregistro;
    Button btnrecuperar;
    Button btnsesion;

    //Test FaceId
    Button btnenrol;
    Button btnverify;

    //Test ARUnity
    Button btnMinigame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnregistro = findViewById(R.id.btnResgistrarse);
        btnrecuperar = findViewById(R.id.btnRecuperarc);
        btnsesion = findViewById(R.id.btnIniciarsesion);

        //Test FaceId
        btnenrol = findViewById(R.id.btnEnrolarRostro);
        btnverify = findViewById(R.id.btnReconocerRostro);

        //Test ARUnity
        btnMinigame = findViewById(R.id.btnMinigame);


        btnregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irReg = new Intent(Login.this, Register.class);
                startActivity(irReg);
            }
        });

        btnrecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irRecuperar = new Intent(Login.this, RecuperarCuenta.class);
                startActivity(irRecuperar);
            }
        });

        btnsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irDash = new Intent(Login.this, Dashboard.class);
                startActivity(irDash);
            }
        });

        btnenrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                if (FaceVault.hasEmbedding(Login.this)) {
                    new androidx.appcompat.app.AlertDialog.Builder(Login.this)
                            .setTitle("Rostro ya registrado")
                            .setMessage("Ya existe un rostro guardado en este dispositivo. ¿Deseas reemplazarlo?")
                            .setPositiveButton("Sí, reemplazar", (dialog, which) -> {
                                openEnrollFlow();
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    openEnrollFlow();
                }

            }
            private void openEnrollFlow() {
                Intent intent;
                if (FaceVault.hasConsent(Login.this)) {
                    intent = new Intent(Login.this, FaceEnrollActivity.class);
                } else {
                    intent = new Intent(Login.this, FaceConsentActivity.class);
                }
                startActivity(intent);
            }
        });

        btnverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                Intent goVerify = new Intent(Login.this, FaceVerifyActivity.class);
                startActivity(goVerify);

            }
        });

        /*btnMinigame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent irMinigame = new Intent(Login.this, com.unity3d.player.UnityPlayerActivity.class);
                Intent irMinigame = new Intent(Login.this, UnityArActivity.class);
                startActivity(irMinigame);
            }
        });*/

        btnMinigame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irMinigame = new Intent(Login.this, UnityArActivity.class);

                String json = "{"
                        + "\"exerciseId\":\"PARABOLIC-001\","
                        + "\"initialPower\":3.5,"
                        + "\"initialAngle\":35.0,"
                        + "\"showTrajectory\":true"
                        + "}";

                irMinigame.putExtra(UnityArActivity.EXTRA_EXERCISE_DATA, json);
                startActivity(irMinigame);
            }
        });

    }

}