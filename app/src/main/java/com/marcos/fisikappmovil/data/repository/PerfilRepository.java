package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.response.PerfilResponse;

import com.marcos.fisikappmovil.remote.response.PerfilUpdateResponse;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilRepository {

    private final FisikappApi api;

    private String buildEmbeddingJson(String base64Embedding) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "facenet");
            json.put("format", "float32_base64_little_endian");
            json.put("dimension", 512);
            json.put("data", base64Embedding);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public PerfilRepository() {
        this.api = RetrofitClient.getApi();
    }

    public void getPerfil(String authorizationHeader, RepositoryCallback<PerfilResponse> callback) {
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            callback.onComplete(AppResult.error("Sesión no válida.", 401));
            return;
        }

        api.getPerfil(authorizationHeader).enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error("No fue posible cargar el perfil.", response.code()));
                    return;
                }

                PerfilResponse body = response.body();

                if (body == null) {
                    callback.onComplete(AppResult.error("Perfil vacío.", response.code()));
                    return;
                }

                callback.onComplete(AppResult.success(body, response.code()));
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                callback.onComplete(AppResult.error("Error de conexión: " + t.getMessage(), 0));
            }
        });
    }

    public void actualizarPerfil(
            String authorizationHeader,
            String base64Embedding,
            RepositoryCallback<PerfilResponse> callback
    ) {
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            callback.onComplete(AppResult.error("Sesión no válida.", 401));
            return;
        }

        if (base64Embedding == null || base64Embedding.trim().isEmpty()) {
            callback.onComplete(AppResult.error("Embedding facial vacío.", 0));
            return;
        }

        String embeddingJson = buildEmbeddingJson(base64Embedding);

        if (embeddingJson == null) {
            callback.onComplete(AppResult.error("No se pudo preparar el embedding facial.", 0));
            return;
        }

        RequestBody autorizacionDatos = RequestBody.create(
                MediaType.parse("text/plain"),
                "true"
        );

        RequestBody embeddingFacial = RequestBody.create(
                MediaType.parse("text/plain"),
                embeddingJson
        );

        api.actualizarPerfilMultipart(
                authorizationHeader,
                autorizacionDatos,
                embeddingFacial
        ).enqueue(new retrofit2.Callback<PerfilUpdateResponse>() {
            @Override
            public void onResponse(
                    retrofit2.Call<PerfilUpdateResponse> call,
                    retrofit2.Response<PerfilUpdateResponse> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error(
                            "No fue posible actualizar el perfil. Código: " + response.code(),
                            response.code()
                    ));
                    return;
                }

                PerfilUpdateResponse body = response.body();

                if (body == null || body.getData() == null) {
                    callback.onComplete(AppResult.error(
                            "Respuesta vacía al actualizar perfil.",
                            response.code()
                    ));
                    return;
                }

                callback.onComplete(AppResult.success(body.getData(), response.code()));
            }

            @Override
            public void onFailure(
                    retrofit2.Call<PerfilUpdateResponse> call,
                    Throwable t
            ) {
                callback.onComplete(AppResult.error(
                        "Error de conexión: " + t.getMessage(),
                        0
                ));
            }
        });
    }

    public void desactivarReconocimientoFacial(
            String authorizationHeader,
            RepositoryCallback<PerfilResponse> callback
    ) {
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            callback.onComplete(AppResult.error("Sesión no válida.", 401));
            return;
        }

        RequestBody autorizacionDatos = RequestBody.create(
                MediaType.parse("text/plain"),
                "false"
        );

        RequestBody embeddingFacial = RequestBody.create(
                MediaType.parse("text/plain"),
                "null"
        );

        api.actualizarPerfilMultipart(
                authorizationHeader,
                autorizacionDatos,
                embeddingFacial
        ).enqueue(new retrofit2.Callback<PerfilUpdateResponse>() {
            @Override
            public void onResponse(
                    retrofit2.Call<PerfilUpdateResponse> call,
                    retrofit2.Response<PerfilUpdateResponse> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error(
                            "No fue posible desactivar el reconocimiento facial. Código: " + response.code(),
                            response.code()
                    ));
                    return;
                }

                PerfilUpdateResponse body = response.body();

                if (body == null || body.getData() == null) {
                    callback.onComplete(AppResult.error(
                            "Respuesta vacía al actualizar perfil.",
                            response.code()
                    ));
                    return;
                }

                callback.onComplete(AppResult.success(body.getData(), response.code()));
            }

            @Override
            public void onFailure(
                    retrofit2.Call<PerfilUpdateResponse> call,
                    Throwable t
            ) {
                callback.onComplete(AppResult.error(
                        "Error de conexión: " + t.getMessage(),
                        0
                ));
            }
        });
    }
}