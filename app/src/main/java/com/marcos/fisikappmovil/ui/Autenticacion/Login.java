package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.security.FaceVault;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;
import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.RecuperacionDeCuenta.RecuperarCuenta;
import com.marcos.fisikappmovil.ui.faceNet.FaceEnrollActivity;
import com.marcos.fisikappmovil.ui.faceNet.FaceVerifyActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    EditText edtCorreo, edtPassword;
    Button btnregistro;
    Button btnrecuperar;
    Button btnsesion;

    //Test FaceId
    Button btnenrol;
    Button btnverify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtPassword = findViewById(R.id.editTextPassword);
        edtCorreo = findViewById(R.id.editTextTextEmailAddress);

        btnregistro = findViewById(R.id.btnResgistrarse);
        btnrecuperar = findViewById(R.id.btnRecuperarc);
        btnsesion = findViewById(R.id.btnIniciarsesion);

        //Test FaceId
        btnenrol = findViewById(R.id.btnEnrolarRostro);
        btnverify = findViewById(R.id.btnReconocerRostro);

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

        btnsesion.setOnClickListener(view -> {

            String correo = edtCorreo.getText().toString();
            String password = edtPassword.getText().toString();

            LoginRequest request = new LoginRequest(correo,password);

            FisikappApi api = RetrofitClient.getClient().create(FisikappApi.class);

            Call<LoginResponse> call = api.login(request);

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        String access = response.body().getToken();
                        guaardarToken(access);

                        Toast.makeText(Login.this, "BIENBENIDO", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "correo o contraseña incorrecto", Toast.LENGTH_SHORT).show();
                    }

                    Intent indas = new Intent(Login.this, Dashboard.class);
                    startActivity(indas);
                }

                private void guaardarToken(String access) {
                    System.out.println("token : "+access);
                }




                @Override
                public void onFailure(Call<LoginResponse> call, Throwable throwable) {

                    Toast.makeText(Login.this, "error"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


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

    }

}