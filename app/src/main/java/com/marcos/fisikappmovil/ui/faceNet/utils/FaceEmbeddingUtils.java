package com.marcos.fisikappmovil.ui.faceNet.utils;

public class FaceEmbeddingUtils {

    public static float[] averageAndNormalize(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return null;

        float[] out = new float[a.length];
        float norm = 0f;

        for (int i = 0; i < a.length; i++) {
            out[i] = (a[i] + b[i]) / 2f;
            norm += out[i] * out[i];
        }

        norm = (float) Math.sqrt(norm);
        if (norm == 0f) return out;

        for (int i = 0; i < out.length; i++) {
            out[i] /= norm;
        }

        return out;
    }
}
