package com.marcos.fisikappmovil.data.repository;

import android.content.Context;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;
import com.marcos.fisikappmovil.model.TokenManager;
import com.marcos.fisikappmovil.remote.response.MobileGroupResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrupoRepository {

    private final FisikappApi api;
    private final TokenManager tokenManager;

    public GrupoRepository(Context context) {
        this.api = RetrofitClient.getApi();
        this.tokenManager = new TokenManager(context);
    }

    public void getMisGrupos(RepositoryCallback<List<GrupoAcademicoItem>> callback) {
        String authHeader = tokenManager.getAuthorizationHeader();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            callback.onComplete(AppResult.error(
                    "Sesión no válida. Inicia sesión nuevamente.",
                    401
            ));
            return;
        }

        api.getMobileGroups(authHeader).enqueue(new Callback<List<MobileGroupResponse>>() {
            @Override
            public void onResponse(
                    Call<List<MobileGroupResponse>> call,
                    Response<List<MobileGroupResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error(
                            buildGroupsHttpErrorMessage(response.code()),
                            response.code()
                    ));
                    return;
                }

                List<MobileGroupResponse> body = response.body();

                if (body == null) {
                    callback.onComplete(AppResult.error(
                            "El servidor respondió vacío.",
                            response.code()
                    ));
                    return;
                }

                callback.onComplete(AppResult.success(mapToGrupoAcademicoItems(body), response.code()));
            }

            @Override
            public void onFailure(Call<List<MobileGroupResponse>> call, Throwable t) {
                String message = t != null && t.getMessage() != null
                        ? "Error de conexión: " + t.getMessage()
                        : "Error de conexión con el servidor.";

                callback.onComplete(AppResult.error(message, -1));
            }
        });
    }

    private List<GrupoAcademicoItem> mapToGrupoAcademicoItems(List<MobileGroupResponse> responseList) {
        List<GrupoAcademicoItem> items = new ArrayList<>();

        if (responseList == null) {
            return items;
        }

        for (MobileGroupResponse response : responseList) {
            if (response == null) continue;

            String nombreGrupo = safe(response.getGrupoNombre());

            if (nombreGrupo.isEmpty()) {
                nombreGrupo = "Grupo académico";
            }

            String detalleGrupo = buildDetalleGrupo(response);
            String instructor = safe(response.getInstructorNombre());

            if (instructor.isEmpty()) {
                instructor = "Instructor no asignado";
            }

            items.add(new GrupoAcademicoItem(
                    response.getGrupoId(),
                    nombreGrupo,
                    detalleGrupo,
                    instructor,
                    response.getTotalLaboratorios(),
                    response.getLaboratoriosActivos(),
                    response.getEntregasPendientes(),
                    response.getEntregasEnviadas(),
                    true
            ));
        }

        return items;
    }

    private String buildDetalleGrupo(MobileGroupResponse response) {
        String grado = safe(response.getGrado());
        String jornada = safe(response.getJornada());

        if (!grado.isEmpty() && !jornada.isEmpty()) {
            return "Grado " + grado + " · " + jornada;
        }

        if (!grado.isEmpty()) {
            return "Grado " + grado;
        }

        if (!jornada.isEmpty()) {
            return jornada;
        }

        return "Grupo académico";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildGroupsHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 401:
            case 403:
                return "Sesión no válida. Inicia sesión nuevamente.";
            case 404:
                return "Servicio de grupos no encontrado.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible cargar los grupos. Código: " + statusCode;
        }
    }
}