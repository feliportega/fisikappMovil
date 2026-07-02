package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.GrupoAcademicoItem;

import java.util.ArrayList;
import java.util.List;

public class GrupoRepository {

    public void getMisGrupos(RepositoryCallback<List<GrupoAcademicoItem>> callback) {
        // MOCK temporal hasta que el backend entregue endpoint real.
        List<GrupoAcademicoItem> grupos = new ArrayList<>();

        grupos.add(new GrupoAcademicoItem(
                1,
                "Física 10A",
                "Institución Educativa",
                "Profesor Juan",
                4,
                2,
                1,
                1,
                true
        ));

        grupos.add(new GrupoAcademicoItem(
                2,
                "Física Experimental",
                "Institución Educativa",
                "Profesora Ana",
                3,
                1,
                2,
                0,
                true
        ));

        grupos.add(new GrupoAcademicoItem(
                3,
                "Laboratorio Mecánica",
                "Institución Educativa",
                "Profesor Camilo",
                2,
                0,
                1,
                1,
                true
        ));

        callback.onComplete(AppResult.success(grupos, 200));
    }
}