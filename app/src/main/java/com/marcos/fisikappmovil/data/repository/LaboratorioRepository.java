package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;
import com.marcos.fisikappmovil.remote.response.GrupoEstudianteResponse;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.remote.response.MobileAssignmentResponse;
import com.marcos.fisikappmovil.remote.response.MobileGroupAssignmentsResponse;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;
import com.marcos.fisikappmovil.remote.response.SubmitLaboratorioResponse;
import com.google.gson.JsonObject;
import com.marcos.fisikappmovil.remote.response.SubmitLaboratorioResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class LaboratorioRepository {

    private final FisikappApi fisikappApi;

    public LaboratorioRepository() {
        this.fisikappApi = RetrofitClient.getApi();
    }

    public void getMisGruposEstudiante(
            String authHeader,
            RepositoryCallback<List<GrupoAcademicoItem>> callback
    ) {
        fisikappApi.getMisGrupos(authHeader).enqueue(new retrofit2.Callback<List<GrupoEstudianteResponse>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<List<GrupoEstudianteResponse>> call,
                    retrofit2.Response<List<GrupoEstudianteResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onComplete(AppResult.error(
                            "No se pudieron cargar los grupos. Código: " + response.code(),
                            response.code()
                    ));
                    return;
                }

                List<GrupoEstudianteResponse> body = response.body();

                if (body == null) {
                    callback.onComplete(AppResult.error("Respuesta vacía del servidor.", response.code()));
                    return;
                }

                List<GrupoAcademicoItem> grupos = new java.util.ArrayList<>();

                for (GrupoEstudianteResponse item : body) {
                    grupos.add(new GrupoAcademicoItem(
                            item.getGrupoId(),
                            safe(item.getGrupoNombre()),
                            "Grado " + safe(item.getGrado()) + " · Jornada " + safe(item.getJornada()),
                            safe(item.getInstructorNombre()),
                            item.getTotalLaboratorios(),
                            item.getEntregasPendientes(),
                            item.getEntregasEnviadas(),
                            item.getCalificacionesPendientes(),
                            item.getLaboratoriosActivos() > 0
                    ));
                }

                callback.onComplete(AppResult.success(grupos, response.code()));
            }

            @Override
            public void onFailure(
                    retrofit2.Call<List<GrupoEstudianteResponse>> call,
                    Throwable t
            ) {
                String mensaje = "No se pudo conectar con el servidor.";

                if (t instanceof java.net.SocketTimeoutException) {
                    mensaje = "El servidor tardó demasiado en responder. Intenta de nuevo.";
                } else if (t instanceof java.net.UnknownHostException) {
                    mensaje = "No hay conexión a internet o no se pudo resolver el servidor.";
                } else if (t instanceof java.net.ConnectException) {
                    mensaje = "No se pudo conectar con el servidor.";
                } else if (t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                    mensaje = "Error de conexión: " + t.getMessage();
                }

                callback.onComplete(AppResult.error(mensaje, -1));
            }
        });
    }

    public void getLaboratoriosPorGrupo(
            String authHeader,
            int grupoId,
            RepositoryCallback<List<LaboratorioAsignadoItem>> callback
    ) {
        fisikappApi.getMobileGroupAssignments(authHeader, grupoId)
                .enqueue(new retrofit2.Callback<MobileGroupAssignmentsResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<MobileGroupAssignmentsResponse> call,
                            retrofit2.Response<MobileGroupAssignmentsResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onComplete(AppResult.error(
                                    buildLaboratoriosHttpErrorMessage(response.code()),
                                    response.code()
                            ));
                            return;
                        }

                        MobileGroupAssignmentsResponse body = response.body();

                        if (body == null || body.getLaboratorios() == null) {
                            callback.onComplete(AppResult.success(
                                    new java.util.ArrayList<>(),
                                    response.code()
                            ));
                            return;
                        }

                        List<LaboratorioAsignadoItem> items = new java.util.ArrayList<>();

                        for (MobileAssignmentResponse lab : body.getLaboratorios()) {
                            if (lab == null) continue;

                            items.add(mapMobileAssignmentToItem(lab, grupoId));
                        }

                        callback.onComplete(AppResult.success(items, response.code()));
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<MobileGroupAssignmentsResponse> call,
                            Throwable t
                    ) {
                        String mensaje = "No se pudo conectar con el servidor.";

                        if (t instanceof java.net.SocketTimeoutException) {
                            mensaje = "El servidor tardó demasiado en responder. Intenta de nuevo.";
                        } else if (t instanceof java.net.UnknownHostException) {
                            mensaje = "No hay conexión a internet o no se pudo resolver el servidor.";
                        } else if (t instanceof java.net.ConnectException) {
                            mensaje = "No se pudo conectar con el servidor.";
                        } else if (t != null && t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                            mensaje = "Error de conexión cargando laboratorios: " + t.getMessage();
                        }

                        callback.onComplete(AppResult.error(mensaje, -1));
                    }
                });
    }

    private LaboratorioAsignadoItem mapMobileAssignmentToItem(
            MobileAssignmentResponse lab,
            int grupoId
    ) {
        String labKey = "";

        if (lab.isTieneAr()) {
            labKey = safe(lab.getLabKey());

            if (labKey.isEmpty()) {
                labKey = "AR";
            }
        }

        String unitySceneName = "";
        String estadoAsignacion = safe(lab.getEstadoAsignacion());
        String estadoEntrega = safe(lab.getEstadoEntrega());
        String calificacionEstado = safe(lab.getCalificacionEstado());

        if (calificacionEstado.isEmpty()) {
            calificacionEstado = "SIN_ENTREGA";
        }

        return new LaboratorioAsignadoItem(
                lab.getAsignacionId(),
                lab.getLaboratorioId(),
                grupoId,
                safe(lab.getTitulo()),
                labKey,
                unitySceneName,
                estadoAsignacion,
                estadoEntrega,
                safe(lab.getFechaInicio()),
                safe(lab.getFechaLimite()),
                0,
                0,
                calificacionEstado
        );
    }

    public void submitMobileAssignment(
            String authHeader,
            int assignmentId,
            JsonObject body,
            RepositoryCallback<SubmitLaboratorioResponse> callback
    ) {
        android.util.Log.d("SUBMIT_DEBUG", "Repository: llamando submitMobileAssignment");
        android.util.Log.d("SUBMIT_DEBUG", "assignmentId=" + assignmentId);
        android.util.Log.d("SUBMIT_DEBUG", "body=" + body.toString());
        fisikappApi.submitMobileAssignment(authHeader, assignmentId, body)
                .enqueue(new retrofit2.Callback<SubmitLaboratorioResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<SubmitLaboratorioResponse> call,
                            retrofit2.Response<SubmitLaboratorioResponse> response
                    ) {
                        android.util.Log.d("SUBMIT_DEBUG", "HTTP code=" + response.code());
                        android.util.Log.d("SUBMIT_DEBUG", "isSuccessful=" + response.isSuccessful());

                        if (!response.isSuccessful()) {
                            try {
                                if (response.errorBody() != null) {
                                    android.util.Log.e("SUBMIT_DEBUG", "errorBody=" + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            callback.onComplete(AppResult.error(
                                    buildSubmitHttpErrorMessage(response.code()),
                                    response.code()
                            ));
                            return;
                        }

                        SubmitLaboratorioResponse responseBody = response.body();

                        if (responseBody == null) {
                            callback.onComplete(AppResult.error(
                                    "El servidor respondió vacío.",
                                    response.code()
                            ));
                            return;
                        }

                        callback.onComplete(AppResult.success(responseBody, response.code()));
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<SubmitLaboratorioResponse> call,
                            Throwable t
                    ) {
                        String mensaje = "No se pudo conectar con el servidor.";

                        if (t instanceof java.net.SocketTimeoutException) {
                            mensaje = "El servidor tardó demasiado en responder. Intenta de nuevo.";
                        } else if (t instanceof java.net.UnknownHostException) {
                            mensaje = "No hay conexión a internet o no se pudo resolver el servidor.";
                        } else if (t instanceof java.net.ConnectException) {
                            mensaje = "No se pudo conectar con el servidor.";
                        } else if (t != null && t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                            mensaje = "Error de conexión enviando laboratorio: " + t.getMessage();
                        }

                        callback.onComplete(AppResult.error(mensaje, -1));
                    }
                });
    }

    public void getMobileResource(
            String authHeader,
            int asignacionId,
            RepositoryCallback<MobileResourceResponse> callback
    ) {
        fisikappApi.getMobileResource(authHeader, asignacionId)
                .enqueue(new retrofit2.Callback<MobileResourceResponse>() {
                    @Override
                    public void onResponse(Call<MobileResourceResponse> call,
                                           Response<MobileResourceResponse> response) {
                        if (!response.isSuccessful()) {
                            callback.onComplete(AppResult.error(
                                    "No se pudo cargar el laboratorio. Código: " + response.code(),
                                    response.code()
                            ));
                            return;
                        }

                        MobileResourceResponse body = response.body();

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
                    public void onFailure(Call<MobileResourceResponse> call, Throwable t) {
                        String message = "No se pudo conectar con el servidor.";

                        if (t != null && t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                            message = "Error de conexión: " + t.getMessage();
                        }

                        callback.onComplete(AppResult.error(message, -1));
                    }
                });
    }

    // heloers
    private String buildLaboratoriosHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 401:
            case 403:
                return "Sesión no válida. Inicia sesión nuevamente.";
            case 404:
                return "No se encontraron laboratorios para este grupo.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible cargar los laboratorios. Código: " + statusCode;
        }
    }

    private String buildSubmitHttpErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "La entrega tiene datos incompletos o inválidos.";
            case 401:
            case 403:
                return "Sesión no válida. Inicia sesión nuevamente.";
            case 404:
                return "No se encontró la asignación del laboratorio.";
            case 409:
                return "La entrega ya fue enviada.";
            case 500:
                return "Error interno del servidor.";
            case 502:
            case 503:
            case 504:
                return "El servidor no está disponible en este momento.";
            default:
                return "No fue posible enviar la entrega. Código: " + statusCode;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}