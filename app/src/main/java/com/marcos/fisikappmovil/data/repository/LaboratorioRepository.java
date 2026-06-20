package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;

import java.util.ArrayList;
import java.util.List;

public class LaboratorioRepository {

    public void getLaboratoriosAsignadosPorGrupo(
            int grupoId,
            RepositoryCallback<List<LaboratorioAsignadoItem>> callback
    ) {
        List<LaboratorioAsignadoItem> laboratorios = new ArrayList<>();

        // Mock principal para el flujo actual.
        laboratorios.add(new LaboratorioAsignadoItem(
                10,
                1,
                grupoId,
                "Tiro parabólico",
                "PARABOLIC-001",
                "ParabolicMotionLab",
                "ABIERTO",
                "PENDIENTE",
                "2026-06-01",
                "2026-06-30",
                0,
                4,
                "PENDIENTE"
        ));

        // Mock adicional para probar estados visuales.
        laboratorios.add(new LaboratorioAsignadoItem(
                11,
                2,
                grupoId,
                "Leyes de Newton",
                "NEWTON-001",
                "NewtonLab",
                "ABIERTO",
                "ENVIADO",
                "2026-06-01",
                "2026-06-25",
                2,
                3,
                "CALIFICACION_PENDIENTE"
        ));

        laboratorios.add(new LaboratorioAsignadoItem(
                12,
                3,
                grupoId,
                "Energía potencial",
                "ENERGY-001",
                "EnergyLab",
                "CERRADO",
                "NO_INICIADO",
                "2026-05-01",
                "2026-05-20",
                0,
                3,
                "SIN_CALIFICACION"
        ));

        callback.onComplete(AppResult.success(laboratorios, 200));
    }
}