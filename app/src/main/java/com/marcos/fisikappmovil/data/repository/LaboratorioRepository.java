package com.marcos.fisikappmovil.data.repository;

import com.marcos.fisikappmovil.data.callback.RepositoryCallback;
import com.marcos.fisikappmovil.data.result.AppResult;
import com.marcos.fisikappmovil.model.LaboratorioAsignadoItem;
import com.marcos.fisikappmovil.model.LaboratorioPasoItem;

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

    public void getPasosLaboratorioMock(
            int asignacionId,
            int laboratorioId,
            RepositoryCallback<List<LaboratorioPasoItem>> callback
    ) {
        List<LaboratorioPasoItem> pasos = new ArrayList<>();

        pasos.add(new LaboratorioPasoItem(
                1,
                "Leer conceptos",
                "Revisa los conceptos básicos, fórmulas y marco teórico del tiro parabólico.",
                LaboratorioPasoItem.TIPO_LECTURA,
                true,
                LaboratorioPasoItem.ESTADO_PENDIENTE
        ));

        pasos.add(new LaboratorioPasoItem(
                2,
                "Responder preguntas",
                "Responde las preguntas de comprensión antes de iniciar la práctica.",
                LaboratorioPasoItem.TIPO_PREGUNTAS,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                3,
                "Práctica experimental",
                "Realiza la práctica con materiales físicos o siguiendo el procedimiento indicado.",
                LaboratorioPasoItem.TIPO_PRACTICA_EXPERIMENTAL,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                4,
                "Registrar datos experimentales",
                "Ingresa las mediciones y observaciones obtenidas en la práctica.",
                LaboratorioPasoItem.TIPO_DATOS_EXPERIMENTALES,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                5,
                "Práctica simulada AR",
                "Ejecuta la práctica simulada en Unity y registra el resultado devuelto por la escena.",
                LaboratorioPasoItem.TIPO_SIMULACION_AR,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                6,
                "Comparar resultados",
                "Compara los resultados experimentales con los resultados de la simulación.",
                LaboratorioPasoItem.TIPO_COMPARACION,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        pasos.add(new LaboratorioPasoItem(
                7,
                "Informe y conclusiones",
                "Revisa el resumen del laboratorio, escribe tus conclusiones y prepara la entrega.",
                LaboratorioPasoItem.TIPO_INFORME,
                true,
                LaboratorioPasoItem.ESTADO_BLOQUEADO
        ));

        callback.onComplete(AppResult.success(pasos, 200));
    }
}