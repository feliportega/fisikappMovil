package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.models.Laboratorio;

import java.util.List;

public class LaboratorioAdapter
        extends RecyclerView.Adapter<LaboratorioAdapter.ViewHolder> {

    List<Laboratorio> lista;

    public LaboratorioAdapter(List<Laboratorio> lista) {

        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_laboratorio, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Laboratorio laboratorio = lista.get(position);

        Log.d("LAB_DEBUG",
                "Titulo: " + laboratorio.getTitulo_lab()
                        + " Codigo: " + laboratorio.getCodigo_lab()
                        + " Resumen: " + laboratorio.getResumen());

        holder.txtTitulo.setText(
                laboratorio.getTitulo_lab());

        holder.txtCodigo.setText(
                laboratorio.getCodigo_lab());

        holder.txtResumen.setText(
                laboratorio.getResumen());
    }

    @Override
    public int getItemCount() {

        return lista.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTitulo, txtCodigo, txtResumen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitulo =
                    itemView.findViewById(R.id.txtTitulo);

            txtCodigo =
                    itemView.findViewById(R.id.txtCodigo);

            txtResumen =
                    itemView.findViewById(R.id.txtResumen);
        }
    }
}