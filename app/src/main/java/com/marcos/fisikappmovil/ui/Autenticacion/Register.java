package com.marcos.fisikappmovil.ui.Autenticacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.data.repository.AuthRepository;

public class Register extends AppCompatActivity {

    private EditText edtNombreCompleto;
    private EditText edtCorreoRegistro;
    private EditText edtIdentificacionRegistro;
    private EditText edtPasswordRegistro;
    private EditText edtConfirmarPasswordRegistro;

    private Button btnCrearCuenta;
    private TextView btnYaTienesCuenta;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository();

        initViews();
        initListeners();
    }

    private void initViews() {
        edtNombreCompleto = findViewById(R.id.edtNombreCompleto);
        edtCorreoRegistro = findViewById(R.id.edtCorreoRegistro);
        edtIdentificacionRegistro = findViewById(R.id.edtIdentificacionRegistro);
        edtPasswordRegistro = findViewById(R.id.edtPasswordRegistro);
        edtConfirmarPasswordRegistro = findViewById(R.id.edtConfirmarPasswordRegistro);

        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnYaTienesCuenta = findViewById(R.id.btnYaTienesCuenta);
    }

    private void initListeners() {
        btnCrearCuenta.setOnClickListener(v -> registrarUsuario());

        btnYaTienesCuenta.setOnClickListener(v -> {
            finish();
        });
    }

    private void registrarUsuario() {
        String nombre = edtNombreCompleto.getText().toString();
        String correo = edtCorreoRegistro.getText().toString();
        String identificacion = edtIdentificacionRegistro.getText().toString();
        String password = edtPasswordRegistro.getText().toString();
        String confirmarPassword = edtConfirmarPasswordRegistro.getText().toString();

        setLoading(true);

        authRepository.register(
                nombre,
                correo,
                password,
                confirmarPassword,
                identificacion,
                result -> runOnUiThread(() -> {
                    setLoading(false);

                    if (!result.isSuccess()) {
                        Toast.makeText(
                                this,
                                result.getErrorMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String message = "Usuario registrado correctamente.";

                    if (result.getData() != null
                            && result.getData().getMessage() != null
                            && !result.getData().getMessage().trim().isEmpty()) {
                        message = result.getData().getMessage();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    finish();
                })
        );
    }

    private void setLoading(boolean loading) {
        btnCrearCuenta.setEnabled(!loading);

        if (loading) {
            btnCrearCuenta.setText("Creando cuenta...");
        } else {
            btnCrearCuenta.setText("Crear cuenta ->");
        }
    }
}