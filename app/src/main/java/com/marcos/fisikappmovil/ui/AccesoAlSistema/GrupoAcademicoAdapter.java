package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;

import java.util.List;

public class GrupoAcademicoAdapter extends RecyclerView.Adapter<GrupoAcademicoAdapter.GrupoViewHolder> {

    public interface OnGrupoClickListener {
        void onGrupoClick(GrupoAcademicoItem grupo);
    }

    private final List<GrupoAcademicoItem> grupos;
    private final OnGrupoClickListener listener;

    public GrupoAcademicoAdapter(List<GrupoAcademicoItem> grupos, OnGrupoClickListener listener) {
        this.grupos = grupos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grupo_academico, parent, false);

        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        GrupoAcademicoItem grupo = grupos.get(position);

        holder.tvGrupoNombre.setText(grupo.getNombre());
        holder.tvGrupoProfesor.setText(grupo.getProfesor());
        holder.tvGrupoInstitucion.setText(grupo.getInstitucion());

        holder.tvGrupoResumen.setText(
                grupo.getTotalActividades() + " actividades · "
                        + grupo.getPendientes() + " pendientes"
        );

        holder.tvPendientes.setText(String.valueOf(grupo.getPendientes()));
        holder.tvEntregadas.setText(String.valueOf(grupo.getEntregadas()));

        holder.btnVerGrupo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGrupoClick(grupo);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGrupoClick(grupo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return grupos != null ? grupos.size() : 0;
    }

    static class GrupoViewHolder extends RecyclerView.ViewHolder {

        TextView tvGrupoNombre;
        TextView tvGrupoProfesor;
        TextView tvGrupoInstitucion;
        TextView tvGrupoResumen;
        TextView tvPendientes;
        TextView tvEntregadas;
        Button btnVerGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGrupoNombre = itemView.findViewById(R.id.tvGrupoNombre);
            tvGrupoProfesor = itemView.findViewById(R.id.tvGrupoProfesor);
            tvGrupoInstitucion = itemView.findViewById(R.id.tvGrupoInstitucion);
            tvGrupoResumen = itemView.findViewById(R.id.tvGrupoResumen);
            tvPendientes = itemView.findViewById(R.id.tvPendientes);
            tvEntregadas = itemView.findViewById(R.id.tvEntregadas);
            btnVerGrupo = itemView.findViewById(R.id.btnVerGrupo);
        }
    }
}