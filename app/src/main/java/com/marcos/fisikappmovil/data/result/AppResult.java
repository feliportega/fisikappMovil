package com.marcos.fisikappmovil.data.result;

public class AppResult<T> {

    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final int statusCode;

    private AppResult(boolean success, T data, String errorMessage, int statusCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    public static <T> AppResult<T> success(T data, int statusCode) {
        return new AppResult<>(true, data, null, statusCode);
    }

    public static <T> AppResult<T> error(String errorMessage, int statusCode) {
        return new AppResult<>(false, null, errorMessage, statusCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
