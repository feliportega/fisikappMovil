package com.marcos.fisikappmovil.ui.Laboratorio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;

import java.util.List;

public class LaboratorioAsignadoAdapter extends RecyclerView.Adapter<LaboratorioAsignadoAdapter.LabViewHolder> {

    public interface OnLaboratorioClickListener {
        void onLaboratorioClick(LaboratorioAsignadoItem laboratorio);
    }

    private final List<LaboratorioAsignadoItem> laboratorios;
    private final OnLaboratorioClickListener listener;

    public LaboratorioAsignadoAdapter(
            List<LaboratorioAsignadoItem> laboratorios,
            OnLaboratorioClickListener listener
    ) {
        this.laboratorios = laboratorios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_laboratorio_asignado, parent, false);

        return new LabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabViewHolder holder, int position) {
        LaboratorioAsignadoItem lab = laboratorios.get(position);

        holder.tvLabTitulo.setText(lab.getTitulo());
        holder.tvLabKey.setText(lab.getLabKey());

        holder.tvLabEstado.setText(
                "Asignación: " + lab.getEstadoAsignacion()
                        + " · Entrega: " + lab.getEstadoEntrega()
        );

        holder.tvLabIntentos.setText(
                "Intentos: " + lab.getIntentosUsados() + "/" + lab.getIntentosMaximos()
        );

        holder.tvLabFecha.setText("Fecha límite: " + lab.getFechaFin());
        holder.tvLabCalificacion.setText("Calificación: " + formatEstadoCalificacion(lab.getCalificacionEstado()));

        if (lab.estaDisponible()) {
            holder.btnVerLaboratorio.setEnabled(true);
            holder.btnVerLaboratorio.setText("Ver laboratorio");
        } else {
            holder.btnVerLaboratorio.setEnabled(true);
            holder.btnVerLaboratorio.setText("Ver detalle");
        }

        holder.btnVerLaboratorio.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLaboratorioClick(lab);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLaboratorioClick(lab);
            }
        });
    }

    @Override
    public int getItemCount() {
        return laboratorios != null ? laboratorios.size() : 0;
    }

    private String formatEstadoCalificacion(String estado) {
        if (estado == null) return "Pendiente";

        switch (estado.toUpperCase()) {
            case "CALIFICACION_PENDIENTE":
                return "Pendiente";
            case "CALIFICADO":
                return "Calificado";
            case "SIN_CALIFICACION":
                return "Sin calificación";
            default:
                return estado;
        }
    }

    static class LabViewHolder extends RecyclerView.ViewHolder {

        TextView tvLabTitulo;
        TextView tvLabKey;
        TextView tvLabEstado;
        TextView tvLabIntentos;
        TextView tvLabFecha;
        TextView tvLabCalificacion;
        Button btnVerLaboratorio;

        public LabViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLabTitulo = itemView.findViewById(R.id.tvLabTitulo);
            tvLabKey = itemView.findViewById(R.id.tvLabKey);
            tvLabEstado = itemView.findViewById(R.id.tvLabEstado);
            tvLabIntentos = itemView.findViewById(R.id.tvLabIntentos);
            tvLabFecha = itemView.findViewById(R.id.tvLabFecha);
            tvLabCalificacion = itemView.findViewById(R.id.tvLabCalificacion);
            btnVerLaboratorio = itemView.findViewById(R.id.btnVerLaboratorio);
        }
    }
}