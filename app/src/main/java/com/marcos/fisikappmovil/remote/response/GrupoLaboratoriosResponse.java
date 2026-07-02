package com.marcos.fisikappmovil.remote.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GrupoLaboratoriosResponse {

    @SerializedName("grupo")
    private GrupoInfoResponse grupo;

    @SerializedName("laboratorios")
    private List<LaboratorioGrupoResponse> laboratorios;

    public GrupoInfoResponse getGrupo() { return grupo; }
    public List<LaboratorioGrupoResponse> getLaboratorios() { return laboratorios; }
}