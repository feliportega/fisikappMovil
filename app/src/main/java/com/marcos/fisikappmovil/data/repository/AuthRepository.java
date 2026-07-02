package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final FisikappApi api;

    public AuthRepository() {
        this.api = RetrofitClient.getApi();
    }

    public void login(String correo, String password, RepositoryCallback<LoginResponse> callback) {
        String cleanCorreo = correo == null ? "" : correo.trim();
        String cleanPassword = password == null ? "" : password.trim();

        if (cleanCorreo.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese el correo electrónico.", 0));
            return;
        }

        if (cleanPassword.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese la contraseña.", 0));
            return;
        }

        LoginRequest request = new LoginRequest(cleanCorreo, cleanPassword);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onComplete(
                            AppResult.error(buildHttpErrorMessage(response.code()), response.code())
                    );
                    return;
                }

                LoginResponse body = response.body();

                if (body == null) {
                    callback.onComplete(
                            AppResult.error("El servidor respondió vacío.", response.code())
                    );
                    return;
                }

                if (!body.hasValidAccessToken()) {
                    callback.onComplete(
                            AppResult.error("La respuesta no contiene token de acceso.", response.code())
                    );
                    return;
                }

                callback.onComplete(AppResult.success(body, response.code()));
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String message = t.getMessage() != null
                        ? "Error de conexión: " + t.getMessage()
                        : "Error de conexión con el servidor.";

                callback.onComplete(AppResult.error(message, 0));
            }
        });
    }

    private String buildHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Solicitud inválida. Revise los datos ingresados.";
            case 401:
            case 403:
                return "Correo o contraseña incorrectos.";
            case 404:
                return "Servicio de login no encontrado.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible iniciar sesión. Código: " + statusCode;
        }
    }
}