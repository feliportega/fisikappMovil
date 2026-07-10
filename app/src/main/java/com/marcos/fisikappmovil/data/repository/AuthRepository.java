package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.request.RegisterRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.remote.response.RegisterResponse;

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

    public void register(
            String nombre,
            String correo,
            String password,
            String confirmPassword,
            String identificacion,
            RepositoryCallback<RegisterResponse> callback
    ) {
        String cleanNombre = nombre == null ? "" : nombre.trim();
        String cleanCorreo = correo == null ? "" : correo.trim();
        String cleanPassword = password == null ? "" : password.trim();
        String cleanConfirmPassword = confirmPassword == null ? "" : confirmPassword.trim();
        String cleanIdentificacion = identificacion == null ? "" : identificacion.trim();

        if (cleanNombre.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese el nombre completo.", 0));
            return;
        }

        if (cleanCorreo.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese el correo electrónico.", 0));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanCorreo).matches()) {
            callback.onComplete(AppResult.error("Ingrese un correo válido.", 0));
            return;
        }

        if (cleanIdentificacion.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese la identificación.", 0));
            return;
        }

        if (cleanPassword.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese la contraseña.", 0));
            return;
        }

        if (cleanPassword.length() < 8) {
            callback.onComplete(AppResult.error("La contraseña debe tener al menos 8 caracteres.", 0));
            return;
        }

        if (!cleanPassword.equals(cleanConfirmPassword)) {
            callback.onComplete(AppResult.error("Las contraseñas no coinciden.", 0));
            return;
        }

        RegisterRequest request = new RegisterRequest(
                cleanNombre,
                cleanCorreo,
                cleanPassword,
                cleanIdentificacion,
                null
        );

        api.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onComplete(
                            AppResult.error(buildRegisterHttpErrorMessage(response.code()), response.code())
                    );
                    return;
                }

                RegisterResponse body = response.body();

                if (body == null) {
                    callback.onComplete(
                            AppResult.error("El servidor respondió vacío.", response.code())
                    );
                    return;
                }

                callback.onComplete(AppResult.success(body, response.code()));
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                String message = t.getMessage() != null
                        ? "Error de conexión: " + t.getMessage()
                        : "Error de conexión con el servidor.";

                callback.onComplete(AppResult.error(message, 0));
            }
        });
    }

    private String buildRegisterHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "No fue posible registrar el usuario. Revise los datos ingresados.";
            case 409:
                return "Ya existe un usuario registrado con estos datos.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible crear la cuenta. Código: " + statusCode;
        }
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