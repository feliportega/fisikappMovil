package com.marcos.fisikappmovil.ui.Autenticacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.ui.AccesoAlSistema.Dashboard;

public class Register extends AppCompatActivity {

    Button btncrearcuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        btncrearcuenta = findViewById(R.id.btnCrearCuenta);
        btncrearcuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irDash = new Intent(Register.this, Dashboard.class);
                startActivity(irDash);
            }
        });

    }
}