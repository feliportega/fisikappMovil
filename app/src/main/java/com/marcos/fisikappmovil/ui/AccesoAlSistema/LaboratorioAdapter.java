package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.Incripcion;

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

        // Extraemos los datos directamente del objeto Inscripcion que viene de tu JSON
        String idInscripcion = "Inscripción N°: " + inscripcion.getId();
        String fecha = "Fecha: " + inscripcion.getFecha_inscripcion();
        String idLab = "Laboratorio ID: " + inscripcion.getLaboratorio();

        // Asignamos los datos directamente a tus TextViews del diseño
        if (holder.txtTitulo != null) holder.txtTitulo.setText(idInscripcion);
        if (holder.txtCodigo != null) holder.txtCodigo.setText(fecha);
        if (holder.txtResumen != null) holder.txtResumen.setText(idLab);
    }

    @Override
    public int getItemCount() {
        return listaInscripciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtCodigo, txtResumen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtCodigo = itemView.findViewById(R.id.txtCodigo);
            txtResumen = itemView.findViewById(R.id.txtResumen);
        }
    }
}