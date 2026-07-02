package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;
import com.marcos.fisikappmovil.remote.response.GrupoEstudianteResponse;

import com.marcos.fisikappmovil.api.FisikappApi;
import com.marcos.fisikappmovil.api.RetrofitClient;
import com.marcos.fisikappmovil.remote.response.GrupoLaboratoriosResponse;
import com.marcos.fisikappmovil.remote.response.LaboratorioGrupoResponse;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class LaboratorioRepository {

    private final FisikappApi fisikappApi;

    public LaboratorioRepository() {
        this.fisikappApi = RetrofitClient.getApi();
    }

    public void getPasosLaboratorioMock(
            int asignacionId,
            int laboratorioId,
            RepositoryCallback<List<LaboratorioPasoItem>> callback
    ) {
        List<LaboratorioPasoItem> pasos = new ArrayList<>();

        pasos.add(new LaboratorioPasoItem(
                1,
                "Leer conceptos",
                "Revisa los conceptos básicos, fórmulas y marco teórico del tiro parabólico.",
                LaboratorioPasoItem.TIPO_LECTURA,
                true,
                LaboratorioPasoItem.ESTADO_PENDIENTE
        ));

        pasos.add(new LaboratorioPasoItem(
                2,
                "Responder preguntas",
                "Responde las preguntas de comprensión antes de iniciar la práctica.",
                LaboratorioPasoItem.TIPO_PREGUNTAS,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                3,
                "Práctica experimental",
                "Realiza la práctica con materiales físicos o siguiendo el procedimiento indicado.",
                LaboratorioPasoItem.TIPO_PRACTICA_EXPERIMENTAL,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                4,
                "Registrar datos experimentales",
                "Ingresa las mediciones y observaciones obtenidas en la práctica.",
                LaboratorioPasoItem.TIPO_DATOS_EXPERIMENTALES,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                5,
                "Práctica simulada AR",
                "Ejecuta la práctica simulada en Unity y registra el resultado devuelto por la escena.",
                LaboratorioPasoItem.TIPO_SIMULACION_AR,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                6,
                "Comparar resultados",
                "Compara los resultados experimentales con los resultados de la simulación.",
                LaboratorioPasoItem.TIPO_COMPARACION,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                7,
                "Informe y conclusiones",
                "Revisa el resumen del laboratorio, escribe tus conclusiones y prepara la entrega.",
                LaboratorioPasoItem.TIPO_INFORME,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        callback.onComplete(AppResult.success(pasos, 200));
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
        fisikappApi.getLaboratoriosPorGrupo(authHeader, grupoId)
                .enqueue(new retrofit2.Callback<GrupoLaboratoriosResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<GrupoLaboratoriosResponse> call,
                            retrofit2.Response<GrupoLaboratoriosResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onComplete(AppResult.error(
                                    "No se pudieron cargar los laboratorios. Código: " + response.code(),
                                    response.code()
                            ));
                            return;
                        }

                        GrupoLaboratoriosResponse body = response.body();

                        if (body == null || body.getLaboratorios() == null) {
                            callback.onComplete(AppResult.success(new java.util.ArrayList<>(), response.code()));
                            return;
                        }

                        List<LaboratorioAsignadoItem> items = new java.util.ArrayList<>();

                        for (LaboratorioGrupoResponse lab : body.getLaboratorios()) {
                            items.add(new LaboratorioAsignadoItem(
                                    lab.getAsignacionId(),
                                    lab.getLaboratorioId(),
                                    grupoId,
                                    safe(lab.getTitulo()),
                                    resolverLabKeyTemporal(lab.getTitulo()),
                                    resolverUnitySceneTemporal(lab.getTitulo()),
                                    safe(lab.getEstadoAsignacion()),
                                    safe(lab.getEstadoEntrega()),
                                    safe(lab.getFechaInicio()),
                                    safe(lab.getFechaLimite()),
                                    0,
                                    4,
                                    safe(lab.getCalificacionEstado())
                            ));
                        }

                        callback.onComplete(AppResult.success(items, response.code()));
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<GrupoLaboratoriosResponse> call,
                            Throwable t
                    ) {
                        callback.onComplete(AppResult.error(
                                "Error de conexión cargando laboratorios: " + t.getMessage(),
                                -1
                        ));
                    }
                });
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String resolverLabKeyTemporal(String titulo) {
        if (titulo == null) return "LAB-GENERICO";

        String t = titulo.toLowerCase();

        if (t.contains("parabólico")) return "PARABOLIC-001";
        if (t.contains("hooke")) return "HOOKE-001";
        if (t.contains("caída")) return "FREE-FALL-001";
        if (t.contains("rectilíneo")) return "MRUV-001";

        return "LAB-GENERICO";
    }

    private String resolverUnitySceneTemporal(String titulo) {
        if (titulo == null) return "";

        String t = titulo.toLowerCase();

        if (t.contains("parabólico")) {
            return "ParabolicMotionLab";
        }

        return "";
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


}