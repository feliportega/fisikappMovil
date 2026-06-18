package com.marcos.fisikappmovil.security;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FaceEmbeddingCodec {

    public static String encode(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (float value : embedding) {
            buffer.putFloat(value);
        }

        return Base64.encodeToString(buffer.array(), Base64.NO_WRAP);
    }

    public static float[] decode(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return null;
        }

        byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);

        if (bytes.length % 4 != 0) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        int length = bytes.length / 4;
        float[] embedding = new float[length];

        for (int i = 0; i < length; i++) {
            embedding[i] = buffer.getFloat();
        }

        return embedding;
    }

    public static boolean isValidEmbedding(float[] embedding) {
        return embedding != null && embedding.length == 512;
    }

    public static boolean isValidBase64Embedding(String base64) {
        float[] embedding = decode(base64);
        return isValidEmbedding(embedding);
    }
}