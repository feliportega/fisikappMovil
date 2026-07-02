package com.marcos.fisikappmovil.data.callback;

import com.marcos.fisikappmovil.data.result.AppResult;

public interface RepositoryCallback<T> {
    void onComplete(AppResult<T> result);
}