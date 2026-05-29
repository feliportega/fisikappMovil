package com.marcos.fisikappmovil.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente central para las conexiones a la API de Fisikapp.
 */
public class RetrofitClient {
    private static Retrofit retrofit = null;

    // URL principal (Producción en Render)
    private static final String BASE_URL = "https://backend-fisikapp.onrender.com/api/";

    // --- OPCIONES PARA PRUEBAS LOCALES (Solo para ensayos) ---
    // private static final String BASE_URL = "http://10.0.2.2:8000/api/"; // Para Emulador
    // private static final String BASE_URL = "http://192.168.1.XX:8000/api/"; // Para Celular (usa la IP de la PC)

    /**
     * Configura y retorna la instancia de Retrofit.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Timeouts de 60 segundos para compensar la lentitud del servidor gratuito
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
