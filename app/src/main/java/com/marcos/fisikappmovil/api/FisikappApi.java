package com.marcos.fisikappmovil.api;

import com.marcos.fisikappmovil.remote.request.LoginRequest;
import com.marcos.fisikappmovil.remote.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FisikappApi {

    @POST("api/users/login/")
    Call<LoginResponse> login(@Body LoginRequest request);

}