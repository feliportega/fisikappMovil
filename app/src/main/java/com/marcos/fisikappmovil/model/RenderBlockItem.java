package com.marcos.fisikappmovil.model;

import java.util.ArrayList;
import java.util.List;

public class RenderBlockItem {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_TITLE = "TITLE";
    public static final String TYPE_LIST = "LIST";
    public static final String TYPE_FORMULA = "FORMULA";
    public static final String TYPE_CARD = "CARD";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_URL = "URL";
    public static final String TYPE_VIDEO = "VIDEO";

    public static final String TYPE_NUMBERED_LIST = "NUMBERED_LIST";

    private final String type;
    private final String title;
    private final String value;
    private final List<String> items;

    public RenderBlockItem(String type, String title, String value, List<String> items) {
        this.type = type;
        this.title = title;
        this.value = value;
        this.items = items == null ? new ArrayList<>() : items;
    }

    public static RenderBlockItem text(String value) {
        return new RenderBlockItem(TYPE_TEXT, null, value, null);
    }

    public static RenderBlockItem title(String value) {
        return new RenderBlockItem(TYPE_TITLE, null, value, null);
    }

    public static RenderBlockItem list(String title, List<String> items) {
        return new RenderBlockItem(TYPE_LIST, title, null, items);
    }

    public static RenderBlockItem formula(String title, String expression, String description) {
        StringBuilder builder = new StringBuilder();

        if (expression != null && !expression.trim().isEmpty()) {
            builder.append(expression.trim());
        }

        if (description != null && !description.trim().isEmpty()) {
            if (builder.length() > 0) builder.append("\n\n");
            builder.append(description.trim());
        }

        return new RenderBlockItem(TYPE_FORMULA, title, builder.toString(), null);
    }

    public static RenderBlockItem card(String title, String value) {
        return new RenderBlockItem(TYPE_CARD, title, value, null);
    }

    public static RenderBlockItem numberedList(String title, List<String> items) {
        return new RenderBlockItem(TYPE_NUMBERED_LIST, title, null, items);
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public List<String> getItems() {
        return items;
    }
}