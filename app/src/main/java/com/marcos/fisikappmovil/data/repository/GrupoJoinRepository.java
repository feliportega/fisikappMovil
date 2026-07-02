package com.marcos.fisikappmovil.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.remote.response.UnirseGrupoResponse;

public class GrupoJoinRepository {

    public void unirseGrupo(String codigo, RepositoryCallback<UnirseGrupoResponse> callback) {
        String cleanCodigo = codigo == null ? "" : codigo.trim();

        if (cleanCodigo.isEmpty()) {
            callback.onComplete(AppResult.error("Ingrese un código de grupo.", 0));
            return;
        }

        if (cleanCodigo.length() < 4) {
            callback.onComplete(AppResult.error("El código ingresado no parece válido.", 0));
            return;
        }

        // MOCK temporal mientras no existe endpoint.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callback.onComplete(
                    AppResult.error(
                            "Código leído correctamente. Falta conectar endpoint de inscripción.",
                            202
                    )
            );
        }, 600);
    }
}