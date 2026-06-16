package com.marcos.fisikappmovil.ui.UnityAR;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class ResultadoUnityActivity extends AppCompatActivity {

    public static final String EXTRA_UNITY_RESULT = "unityResult";

    private TextView txtResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_unity);

        txtResultado = findViewById(R.id.txtResultadoUnity);

        String resultJson = getIntent().getStringExtra(EXTRA_UNITY_RESULT);

        if (resultJson == null || resultJson.trim().isEmpty()) {
            txtResultado.setText("No se recibió resultado desde Unity.");
        } else {
            txtResultado.setText("Resultado recibido desde Unity:\n\n" + resultJson);
        }
    }
}
