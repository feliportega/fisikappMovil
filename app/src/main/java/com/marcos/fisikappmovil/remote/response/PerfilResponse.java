package com.marcos.fisikappmovil.remote.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class PerfilResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("rol")
    private String rol;

    @SerializedName("estado")
    private boolean estado;

    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    @SerializedName("identificacion")
    private String identificacion;

    @SerializedName("institucion")
    private String institucion;

    @SerializedName("foto")
    private String foto;

    @SerializedName("foto_url")
    private String fotoUrl;

    @SerializedName("embedding_facial")
    private JsonElement embeddingFacial;

    @SerializedName("autorizacion_datos")
    private boolean autorizacionDatos;

    @SerializedName("last_login")
    private String lastLogin;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getFoto() {
        return foto;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public JsonElement getEmbeddingFacial() {
        return embeddingFacial;
    }

    public boolean isAutorizacionDatos() {
        return autorizacionDatos;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public boolean hasBackendFaceEmbedding() {
        if (embeddingFacial == null || embeddingFacial.isJsonNull()) {
            return false;
        }

        if (embeddingFacial.isJsonObject()) {
            JsonElement data = embeddingFacial.getAsJsonObject().get("data");
            return data != null
                    && !data.isJsonNull()
                    && !data.getAsString().trim().isEmpty();
        }

        return false;
    }
}