package com.marcos.fisikappmovil.mapper;

import com.marcos.fisikappmovil.model.RenderBlockItem;
import com.marcos.fisikappmovil.remote.response.MobileConceptResponse;
import com.marcos.fisikappmovil.remote.response.MobileContentBlockResponse;
import com.marcos.fisikappmovil.remote.response.MobileFormulaResponse;
import com.marcos.fisikappmovil.remote.response.MobileObjectiveSpecificResponse;
import com.marcos.fisikappmovil.remote.response.MobileProcedureStepResponse;
import com.marcos.fisikappmovil.remote.response.MobileStepResponse;

import java.util.ArrayList;
import java.util.List;

public class StepRenderMapper {

    public List<RenderBlockItem> map(MobileStepResponse step) {
        List<RenderBlockItem> blocks = new ArrayList<>();

        if (step == null) {
            blocks.add(RenderBlockItem.text("No se encontró contenido para este paso."));
            return blocks;
        }

        String type = safe(step.getType());

        switch (type) {
            case "INTRODUCTION":
            case "THEORY":
                mapContentBlocks(step, blocks);
                break;

            case "OBJECTIVES":
                mapObjectives(step, blocks);
                break;

            case "CONCEPTS":
                mapConcepts(step, blocks);
                break;

            case "PROCEDURES":
                mapProcedures(step, blocks);
                break;

            case "FORMULAS":
                mapFormulas(step, blocks);
                break;

            default:
                blocks.add(RenderBlockItem.text("Este tipo de contenido todavía no está disponible en la app."));
                break;
        }

        if (blocks.isEmpty()) {
            blocks.add(RenderBlockItem.text("Este paso no tiene contenido disponible."));
        }

        return blocks;
    }

    private void mapContentBlocks(MobileStepResponse step, List<RenderBlockItem> blocks) {
        if (step.getContent() == null) return;

        for (MobileContentBlockResponse content : step.getContent()) {
            if (content == null) continue;

            String type = safe(content.getType());

            if ("TEXT".equals(type)) {
                blocks.add(RenderBlockItem.text(content.getValue()));
            } else {
                blocks.add(RenderBlockItem.card(type, content.getValue()));
            }
        }
    }

    private void mapObjectives(MobileStepResponse step, List<RenderBlockItem> blocks) {
        if (step.getGeneral() != null && notEmpty(step.getGeneral().getDescription())) {
            blocks.add(RenderBlockItem.card(
                    "Objetivo general",
                    step.getGeneral().getDescription()
            ));
        }

        if (step.getSpecifics() != null && !step.getSpecifics().isEmpty()) {
            List<String> items = new ArrayList<>();

            for (MobileObjectiveSpecificResponse specific : step.getSpecifics()) {
                if (specific != null && notEmpty(specific.getDescription())) {
                    items.add(specific.getDescription());
                }
            }

            if (!items.isEmpty()) {
                blocks.add(RenderBlockItem.list("Objetivos específicos", items));
            }
        }
    }

    private void mapConcepts(MobileStepResponse step, List<RenderBlockItem> blocks) {
        if (step.getConcepts() == null) return;

        for (MobileConceptResponse concept : step.getConcepts()) {
            if (concept == null) continue;

            StringBuilder value = new StringBuilder();

            if (notEmpty(concept.getDescription())) {
                value.append(concept.getDescription());
            }

            if (notEmpty(concept.getExample())) {
                if (value.length() > 0) value.append("\n\n");
                value.append("Ejemplo: ").append(concept.getExample());
            }

            if (notEmpty(concept.getType())) {
                if (value.length() > 0) value.append("\n\n");
                value.append("Tipo: ").append(concept.getType());
            }

            blocks.add(RenderBlockItem.card(
                    safe(concept.getName()),
                    value.toString()
            ));
        }
    }

    private void mapFormulas(MobileStepResponse step, List<RenderBlockItem> blocks) {
        if (step.getFormulas() == null) return;

        for (MobileFormulaResponse formula : step.getFormulas()) {
            if (formula == null) continue;

            blocks.add(RenderBlockItem.formula(
                    formula.getName(),
                    formula.getExpression(),
                    formula.getDescription()
            ));
        }
    }

    private void mapProcedures(MobileStepResponse step, List<RenderBlockItem> blocks) {
        if (step.getProcedureSteps() == null || step.getProcedureSteps().isEmpty()) {
            blocks.add(RenderBlockItem.text("No hay procedimiento disponible."));
            return;
        }

        List<String> items = new ArrayList<>();

        for (MobileProcedureStepResponse procedure : step.getProcedureSteps()) {
            if (procedure == null) continue;

            String description = procedure.getDescription();

            if (description != null && !description.trim().isEmpty()) {
                int number = procedure.getNumber();

                items.add(description.trim());
            }
        }

        if (items.isEmpty()) {
            blocks.add(RenderBlockItem.text("No hay procedimiento disponible."));
            return;
        }

        blocks.add(RenderBlockItem.numberedList("Procedimiento", items));
    }
    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}