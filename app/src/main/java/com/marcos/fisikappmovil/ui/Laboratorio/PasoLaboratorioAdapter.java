package com.marcos.fisikappmovil.ui.Laboratorio;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;

import java.util.List;

public class PasoLaboratorioAdapter extends RecyclerView.Adapter<PasoLaboratorioAdapter.PasoViewHolder> {

    public interface OnPasoClickListener {
        void onPasoClick(LaboratorioPasoItem paso);
    }

    private final List<LaboratorioPasoItem> pasos;
    private final OnPasoClickListener listener;

    public PasoLaboratorioAdapter(List<LaboratorioPasoItem> pasos, OnPasoClickListener listener) {
        this.pasos = pasos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PasoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso_laboratorio, parent, false);
        return new PasoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasoViewHolder holder, int position) {
        LaboratorioPasoItem paso = pasos.get(position);

        holder.tvNumeroPaso.setText(String.valueOf(paso.getOrden()));
        holder.tvTituloPaso.setText(paso.getTitulo());
        holder.tvDescripcionPaso.setText(paso.getDescripcion());
        holder.tvEstadoPaso.setText(formatEstado(paso.getEstado()));

        boolean esUltimo = position == pasos.size() - 1;
        holder.viewLineaPaso.setVisibility(esUltimo ? View.INVISIBLE : View.VISIBLE);

        aplicarEstiloEstado(holder, paso);

        holder.cardContenidoPaso.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPasoClick(paso);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pasos != null ? pasos.size() : 0;
    }

    private String formatEstado(String estado) {
        if (estado == null) return "Pendiente";

        switch (estado.toUpperCase()) {
            case LaboratorioPasoItem.ESTADO_COMPLETADO:
                return "Completado";
            case LaboratorioPasoItem.ESTADO_EN_PROGRESO:
                return "En progreso";
            case LaboratorioPasoItem.ESTADO_BLOQUEADO:
                return "Bloqueado";
            case LaboratorioPasoItem.ESTADO_PENDIENTE:
            default:
                return "Pendiente";
        }
    }

    private void aplicarEstiloEstado(PasoViewHolder holder, LaboratorioPasoItem paso) {
        String estado = paso.getEstado();

        if (LaboratorioPasoItem.ESTADO_BLOQUEADO.equalsIgnoreCase(estado)) {
            holder.tvNumeroPaso.setAlpha(0.45f);
            holder.tvTituloPaso.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvDescripcionPaso.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvEstadoPaso.setTextColor(Color.parseColor("#64748B"));
            holder.cardContenidoPaso.setAlpha(0.65f);
            return;
        }

        if (LaboratorioPasoItem.ESTADO_COMPLETADO.equalsIgnoreCase(estado)) {
            holder.tvTituloPaso.setTextColor(Color.parseColor("#15803D"));
            holder.tvDescripcionPaso.setTextColor(Color.parseColor("#475569"));
            holder.tvEstadoPaso.setTextColor(Color.parseColor("#15803D"));
            holder.cardContenidoPaso.setAlpha(1f);
            return;
        }

        holder.tvNumeroPaso.setAlpha(1f);
        holder.tvTituloPaso.setTextColor(Color.parseColor("#001B6B"));
        holder.tvDescripcionPaso.setTextColor(Color.parseColor("#777777"));
        holder.tvEstadoPaso.setTextColor(Color.parseColor("#001B6B"));
        holder.cardContenidoPaso.setAlpha(1f);
    }

    static class PasoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNumeroPaso;
        TextView tvTituloPaso;
        TextView tvDescripcionPaso;
        TextView tvEstadoPaso;
        View viewLineaPaso;
        LinearLayout cardContenidoPaso;

        public PasoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNumeroPaso = itemView.findViewById(R.id.tvNumeroPaso);
            tvTituloPaso = itemView.findViewById(R.id.tvTituloPaso);
            tvDescripcionPaso = itemView.findViewById(R.id.tvDescripcionPaso);
            tvEstadoPaso = itemView.findViewById(R.id.tvEstadoPaso);
            viewLineaPaso = itemView.findViewById(R.id.viewLineaPaso);
            cardContenidoPaso = itemView.findViewById(R.id.cardContenidoPaso);
        }
    }
}