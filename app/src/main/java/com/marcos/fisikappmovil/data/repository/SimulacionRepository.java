package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.response.MobileSimulationResponse;

import retrofit2.Call;
import retrofit2.Response;

public class SimulacionRepository {

    private final FisikappApi fisikappApi;

    public SimulacionRepository() {
        this.fisikappApi = RetrofitClient.getApi();
    }

    public void getMobileSimulationConfig(
            String authHeader,
            int arId,
            RepositoryCallback<MobileSimulationResponse> callback
    ) {
        fisikappApi.getMobileArConfig(authHeader, arId)
                .enqueue(new retrofit2.Callback<MobileSimulationResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<MobileSimulationResponse> call,
                            retrofit2.Response<MobileSimulationResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onComplete(AppResult.error(
                                    "No se pudo cargar la configuración AR. Código: " + response.code(),
                                    response.code()
                            ));
                            return;
                        }

                        MobileSimulationResponse body = response.body();

                        if (body == null) {
                            callback.onComplete(AppResult.error(
                                    "Respuesta vacía del servidor.",
                                    response.code()
                            ));
                            return;
                        }

                        callback.onComplete(AppResult.success(body, response.code()));
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<MobileSimulationResponse> call,
                            Throwable t
                    ) {
                        String message = "No se pudo conectar con el servidor.";

                        if (t != null && t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                            message = "Error de conexión: " + t.getMessage();
                        }

                        callback.onComplete(AppResult.error(message, -1));
                    }
                });
    }

    private String leerError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String error = response.errorBody().string();

                if (error != null && !error.trim().isEmpty()) {
                    return " - " + error;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}