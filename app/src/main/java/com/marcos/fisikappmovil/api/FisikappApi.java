package com.marcos.fisikappmovil.api;

import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.request.PerfilUpdateRequest;
import com.marcos.fisikappmovil.remote.response.AsignacionDetalleResponse;
import com.marcos.fisikappmovil.remote.response.GrupoEstudianteResponse;
import com.marcos.fisikappmovil.remote.response.GrupoLaboratoriosResponse;
import com.marcos.fisikappmovil.remote.response.LaboratorioEstudianteDetalleResponse;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.remote.response.MobileResourceResponse;
import com.marcos.fisikappmovil.remote.response.MobileSimulationResponse;
import com.marcos.fisikappmovil.remote.response.PerfilResponse;
import com.marcos.fisikappmovil.remote.response.PerfilUpdateResponse;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface FisikappApi {

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/users/perfil/")
    Call<PerfilResponse> getPerfil(
            @Header("Authorization") String token
    );

    // hihhi
    @Multipart
    @PATCH("api/users/perfil/")
    Call<PerfilUpdateResponse> actualizarPerfilMultipart(
            @Header("Authorization") String token,
            @Part("autorizacion_datos") RequestBody autorizacionDatos,
            @Part("embedding_facial") RequestBody embeddingFacial
    );

    @GET("api/inscripciones/mis-grupos/")
    Call<List<GrupoEstudianteResponse>> getMisGrupos(
            @Header("Authorization") String token
    );

    @GET("api/estudiante/grupos/{grupo_id}/laboratorios/")
    Call<GrupoLaboratoriosResponse> getLaboratoriosPorGrupo(
            @Header("Authorization") String token,
            @Path("grupo_id") int grupoId
    );

    @GET("api/mobile/resources/{asignacion_id}/")
    Call<MobileResourceResponse> getMobileResource(
            @Header("Authorization") String token,
            @Path("asignacion_id") int asignacionId
    );

    @GET("api/mobile/simulation/{simulationId}/")
    Call<MobileSimulationResponse> getMobileSimulationConfig(
            @Header("Authorization") String authHeader,
            @Path("simulationId") int simulationId
    );

    @GET("api/estudiante/asignaciones/{asignacion_id}/detalle/")
    Call<AsignacionDetalleResponse> getDetalleAsignacion(
            @Header("Authorization") String token,
            @Path("asignacion_id") int asignacionId
    );

    @GET("api/laboratorios-estudiante/{laboratorio_id}/")
    Call<LaboratorioEstudianteDetalleResponse> getLaboratorioEstudianteDetalle(
            @Header("Authorization") String token,
            @Path("laboratorio_id") int laboratorioId
    );

}