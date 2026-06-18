package com.marcos.fisikappmovil.api;

import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.request.PerfilUpdateRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;
import com.marcos.fisikappmovil.remote.response.PerfilResponse;
import com.marcos.fisikappmovil.remote.response.PerfilUpdateResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface FisikappApi {

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/users/perfil/")
    Call<PerfilResponse> getPerfil(
            @Header("Authorization") String token
    );

    @Multipart
    @PATCH("api/users/perfil/")
    Call<PerfilUpdateResponse> actualizarPerfilMultipart(
            @Header("Authorization") String token,
            @Part("autorizacion_datos") RequestBody autorizacionDatos,
            @Part("embedding_facial") RequestBody embeddingFacial
    );

}