package com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.marcos.fisikappmovil.R;

public class InformeLaboratorio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // El error era que el nombre del layout no coincidía con el archivo XML.
        // He cambiado activity_informe_laboratorio por activity_informe_final.
        setContentView(R.layout.activity_informe_final);

        // Referencias a los componentes de la interfaz
        ImageView btnBack = findViewById(R.id.btnBack);
        
        // Buscamos los contenedores por su ID en el XML
        // Nota: Asegúrate de que estos IDs existan en activity_informe_final.xml
        View layoutMateriales = findViewById(R.id.layoutMateriales); // Si no existe lo ignorará
        View layoutObservaciones = findViewById(R.id.layoutObservaciones);
        View layoutConclusiones = findViewById(R.id.layoutConclusiones);
        Button btnEnviar = findViewById(R.id.btnEnviar);

        // Configuración de clics
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (layoutObservaciones != null) {
            layoutObservaciones.setOnClickListener(v -> {
                Intent intent = new Intent(InformeLaboratorio.this, segundaPantalla.class);
                startActivity(intent);
            });
        }

        if (layoutConclusiones != null) {
            layoutConclusiones.setOnClickListener(v -> {
                Intent intent = new Intent(InformeLaboratorio.this, tercerapantalla.class);
                startActivity(intent);
            });
        }

        if (btnEnviar != null) {
            btnEnviar.setOnClickListener(v -> {
                Intent intent = new Intent(InformeLaboratorio.this, cuartaPantalla.class);
                startActivity(intent);
            });
        }
    }
}