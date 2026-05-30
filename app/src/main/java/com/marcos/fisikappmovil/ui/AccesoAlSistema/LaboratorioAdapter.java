package com.marcos.fisikappmovil.ui.AccesoAlSistema;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marcos.fisikappmovil.R;
import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.models.Incripcion;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaboratorioAdapter extends RecyclerView.Adapter<LaboratorioAdapter.ViewHolder> {

    private List<Incripcion> listaInscripciones;
    private FisikappApi api;

    public LaboratorioAdapter(List<Incripcion> listaInscripciones) {
        this.listaInscripciones = listaInscripciones;
        this.api = RetrofitClient.getClient().create(FisikappApi.class);
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
        int idLaboratorio = inscripcion.getLaboratorio();

        // Ponemos textos temporales de carga mientras llega la info de la API
        holder.txtTitulo.setText("Cargando laboratorio...");
        holder.txtCodigo.setText("");
        holder.txtResumen.setText("");

        // Hacemos la petición secundaria importando com.google.gson.JsonObject
        api.getLaboratorioPorId(idLaboratorio).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                if (response.isSuccessful() && response.body() != null) {
                    com.google.gson.JsonObject labJson = response.body();

                    // Extraemos los textos directamente del JSON usando las llaves de tu backend
                    // Si en tu base de datos de Django los campos se llaman diferente (ej: "titulo" o "codigo"), cámbialos aquí dentro de las comillas
                    String titulo = labJson.has("titulo_lab") && !labJson.get("titulo_lab").isJsonNull() ? labJson.get("titulo_lab").getAsString() : "Sin título";
                    String codigo = labJson.has("codigo_lab") && !labJson.get("codigo_lab").isJsonNull() ? labJson.get("codigo_lab").getAsString() : "Sin código";
                    String resumen = labJson.has("resumen") && !labJson.get("resumen").isJsonNull() ? labJson.get("resumen").getAsString() : "Sin resumen";

                    // Asignamos los datos directamente a tus TextViews
                    if (holder.txtTitulo != null) holder.txtTitulo.setText(titulo);
                    if (holder.txtCodigo != null) holder.txtCodigo.setText(codigo);
                    if (holder.txtResumen != null) holder.txtResumen.setText(resumen);
                } else {
                    if (holder.txtTitulo != null) holder.txtTitulo.setText("Error al cargar datos");
                    Log.e("ADAPTER_API", "Código de error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                if (holder.txtTitulo != null) holder.txtTitulo.setText("Error de conexión");
                Log.e("ADAPTER_API", "Error: " + t.getMessage());
            }
        });
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