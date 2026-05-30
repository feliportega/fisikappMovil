package com.marcos.fisikappmovil.api;

import com.marcos.fisikappmovil.models.Conclusion;
import com.marcos.fisikappmovil.models.Incripcion;
import com.marcos.fisikappmovil.models.Informe;
import com.marcos.fisikappmovil.models.Recomendacion;
import com.marcos.fisikappmovil.models.Resultado;
import com.marcos.fisikappmovil.remote.request.EmailRequest;
import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.request.RegisterRequest;
import com.marcos.fisikappmovil.remote.request.ResetPasswordRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.models.UnirLaboratorio;
import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface FisikappApi {

    // Informes
    @GET("api/informes/")
    Call<List<Informe>> getInformes();

    @POST("api/informes/")
    Call<Informe> crearInforme(@Body Informe informe);

    @GET("api/informes/{id}/")
    Call<Informe> getInforme(@Path("id") int id);

    @PUT("api/informes/{id}/")
    Call<Informe> actualizarInforme(
            @Path("id") int id,
            @Body Informe informe
    );

    @PATCH("api/informes/{id}/")
    Call<Informe> parchearInforme(
            @Path("id") int id,
            @Body Informe informe
    );

    @DELETE("api/informes/{id}/")
    Call<Void> eliminarInforme(@Path("id") int id);

    // Resultados
    @GET("api/resultados/")
    Call<List<Resultado>> getResultados();

    @POST("api/resultados/")
    Call<Resultado> crearResultado(@Body Resultado resultado);

    @GET("api/resultados/{id}/")
    Call<Resultado> getResultado(@Path("id") int id);

    // Conclusiones
    @GET("api/conclusiones/")
    Call<List<Conclusion>> getConclusiones();

    @POST("api/conclusiones/")
    Call<Conclusion> crearConclusion(@Body Conclusion conclusion);

    @GET("api/conclusiones/{id}/")
    Call<Conclusion> getConclusion(@Path("id") int id);

    // Recomendaciones
    @GET("api/recomendaciones/")
    Call<List<Recomendacion>> getRecomendaciones();

    @POST("api/recomendaciones/")
    Call<Recomendacion> crearRecomendacion(
            @Body Recomendacion recomendacion
    );

    @GET("api/recomendaciones/{id}/")
    Call<Recomendacion> getRecomendacion(
            @Path("id") int id
    );

    // Laboratorios (¡Esta es la que usamos para el Dashboard!)
    @GET("inscripciones/mis-laboratorios/")
    Call<List<Incripcion>> getMisLaboratorios(
            @Header("Authorization") String token
    );

    // Detalles del Laboratorio (¡Esta es la que llama el Adapter internamente!)
    @GET("api/laboratorio/{id}/")
    Call<com.google.gson.JsonObject> getLaboratorioPorId(
            @Path("id") int id
    );

    // Unirse a laboratorio

    @POST("inscribir/")
    Call<UnirLaboratorio> postUnirlaboratorio(
            @Header("Authorization") String token,
            @Body UnirLaboratorio unirLaboratorio
    );

    // --- MÉTODOS DE AUTENTICACIÓN (LOGIN, REGISTER, ETC) ---
    @POST("users/register/")
    Call<Void> register(@Body RegisterRequest request);

    @POST("users/recuperar-contrasena/")
    Call<Void> recuperarContrasena(@Body EmailRequest request);

    @POST("users/restablecer-contrasena/")
    Call<Void> restablecerContrasena(@Body ResetPasswordRequest request);


    //login
    @POST("users/login/")
    Call<LoginResponse> login(@Body LoginRequest request);
}