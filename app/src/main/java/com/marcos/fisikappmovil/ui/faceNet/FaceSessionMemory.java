package com.marcos.fisikappmovil.ui.faceNet;

public class FaceSessionMemory {
    private static float[] enrolledEmbedding;

    public static void saveEmbedding(float[] embedding) {
        enrolledEmbedding = embedding;
    }

    public static float[] getEmbedding() {
        return enrolledEmbedding;
    }

    public static boolean hasEmbedding() {
        return enrolledEmbedding != null && enrolledEmbedding.length > 0;
    }

    public static void clear() {
        enrolledEmbedding = null;
    }
}