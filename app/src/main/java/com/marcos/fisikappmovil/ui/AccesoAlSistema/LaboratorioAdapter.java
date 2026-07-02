package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.Incripcion;
import com.marcos.fisikappmovil.ui.MonitorDeAprendizajeEstudiante.ViewsLaboratorio;

import java.util.List;

public class LaboratorioAdapter extends RecyclerView.Adapter<LaboratorioAdapter.ViewHolder> {

    private List<Incripcion> listaInscripciones;

    // El constructor ahora solo necesita la lista, ya no requiere la API ni el Token
    public LaboratorioAdapter(List<Incripcion> listaInscripciones) {
        this.listaInscripciones = listaInscripciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_laboratorio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incripcion inscripcion = listaInscripciones.get(position);

        // Extraemos los datos del objeto
        int idInscripcion = inscripcion.getId();
        int idLab = inscripcion.getLaboratorio();
        String fecha = inscripcion.getFecha_inscripcion();

        // Asignamos los datos a los TextViews
        if (holder.txtTitulo != null) holder.txtTitulo.setText("Inscripción N°: " + idInscripcion);
        if (holder.txtCodigo != null) holder.txtCodigo.setText("Fecha: " + fecha);
        if (holder.txtResumen != null) holder.txtResumen.setText("Laboratorio ID: " + idLab);

        // --- NUEVA LÓGICA: Configurar el botón Iniciar ---
        holder.btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtenemos el contexto desde la vista del ítem
                android.content.Context context = view.getContext();

                // Creamos el Intent para ir a la vista del laboratorio
                // Nota: Cámbialo por ViewsLaboratorio.class si esa es tu pantalla de destino
                android.content.Intent intent = new android.content.Intent(context, ViewsLaboratorio.class);

                // Pasamos los datos clave que el siguiente Activity va a necesitar para hacer sus consultas
                intent.putExtra("LABORATORIO_ID", idLab);
                intent.putExtra("INSCRIPCION_ID", idInscripcion);

                // Iniciamos la nueva actividad
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaInscripciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtCodigo, txtResumen;
        View btnIniciar; // Declaramos el botón (puede ser un Button, un CardView o el layout que uses para "Iniciar")

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtCodigo = itemView.findViewById(R.id.txtCodigo);
            txtResumen = itemView.findViewById(R.id.txtResumen);

            // BUSCAMOS EL BOTÓN: Asegúrate de que el id en tu XML (activity_item_laboratorio)
            // se llame exactamente igual (por ejemplo, R.id.btnIniciar o R.id.btn_iniciar)
            btnIniciar = itemView.findViewById(R.id.btnIniciar);
        }
    }
}