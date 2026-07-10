package com.marcos.fisikappmovil.data.repository;

import android.content.Context;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.request.JoinGroupRequest;
import com.marcos.fisikappmovil.remote.response.JoinGroupResponse;
import com.marcos.fisikappmovil.model.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrupoJoinRepository {

    private final FisikappApi api;
    private final TokenManager tokenManager;

    public GrupoJoinRepository(Context context) {
        this.api = RetrofitClient.getApi();
        this.tokenManager = new TokenManager(context);
    }

    public void unirseGrupo(
            String codigo,
            RepositoryCallback<JoinGroupResponse> callback
    ) {
        String cleanCodigo = codigo == null ? "" : codigo.trim();

        if (cleanCodigo.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese el código del grupo.", 0));
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            callback.onComplete(AppResult.error("Sesión no válida. Inicia sesión nuevamente.", 401));
            return;
        }

        JoinGroupRequest request = new JoinGroupRequest(cleanCodigo);

        api.joinMobileGroup(authHeader, request).enqueue(new Callback<JoinGroupResponse>() {
            @Override
            public void onResponse(
                    Call<JoinGroupResponse> call,
                    Response<JoinGroupResponse> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error(
                            buildJoinGroupHttpErrorMessage(response.code()),
                            response.code()
                    ));
                    return;
                }

                JoinGroupResponse body = response.body();

                if (body == null) {
                    callback.onComplete(AppResult.error(
                            "El servidor respondió vacío.",
                            response.code()
                    ));
                    return;
                }

                callback.onComplete(AppResult.success(body, response.code()));
            }

            @Override
            public void onFailure(Call<JoinGroupResponse> call, Throwable t) {
                String message = t != null && t.getMessage() != null
                        ? "Error de conexión: " + t.getMessage()
                        : "Error de conexión con el servidor.";

                callback.onComplete(AppResult.error(message, 0));
            }
        });
    }

    private String buildJoinGroupHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Código de grupo inválido o incompleto.";
            case 401:
            case 403:
                return "Sesión no válida. Inicia sesión nuevamente.";
            case 404:
                return "No se encontró un grupo con ese código.";
            case 409:
                return "Ya perteneces a este grupo.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible unirse al grupo. Código: " + statusCode;
        }
    }
}