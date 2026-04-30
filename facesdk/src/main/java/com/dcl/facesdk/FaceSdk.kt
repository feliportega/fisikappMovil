package com.dcl.facesdk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.dcl.facesdk.detector.FaceDetectorEngine
import com.dcl.facesdk.embeddings.FaceNetEngine
import com.dcl.facesdk.liveness.SpoofDetector
import com.dcl.facesdk.liveness.SpoofResult
import com.dcl.facesdk.data.CosineResult
import com.dcl.facesdk.utils.format
import com.dcl.facesdk.utils.mirroredToNonMirrored
import com.dcl.facesdk.utils.cosineSimilarity
import com.dcl.facesdk.utils.imageProxyToBitmapRGBA
import com.dcl.facesdk.utils.safeCropWithMargin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dcl.facesdk.utils.prepareForEmbedding

import android.util.Log

data class FaceSdkResult(
    val name: String? = null,
    val similarity: Float? = null,
    val bbox: Rect,
    val spoof: SpoofResult? = null,
    val embedding: FloatArray? = null,
    // ✅ agregado para acceso desde UI
)

class FaceSdk(
    private val context: Context,
    spoofModelA: String = "spoof_model_scale_2_7.tflite",
    spoofModelB: String = "spoof_model_scale_4_0.tflite",
    spoofInputSize: Int = 80,
    liveIndex: Int = 1
) {
    /**
     * Último frame analizado por CameraX, en formato Bitmap.
     * Se mantiene actualizado en cada llamada a analyzeFrame().
     *
     * Usado por IdentifyLiveScreen para generar embeddings rápidos
     * sin necesidad de volver a convertir el ImageProxy.
     */
    @Volatile
    var lastFrameBitmap: Bitmap? = null
        private set
    private val faceDetector = FaceDetectorEngine(context)
    private val spoofDetector = SpoofDetector(context, spoofModelA, spoofModelB, spoofInputSize, liveIndex)
    private val faceNetEngine = FaceNetEngine(context)

    // ============================================================
    //  ANALYZE FRAME — Flujo en vivo (preview)
    // ============================================================
    suspend fun analyzeFrame(
        image: ImageProxy,
        isFrontCamera: Boolean = true,
        doSpoof: Boolean = false,
        doEmbedding: Boolean = false
    ): List<FaceSdkResult> = withContext(Dispatchers.Default) {

        // Convertir a Bitmap rotado (no espejo)
        val frame = imageProxyToBitmapRGBA(image)
        lastFrameBitmap = frame // ✅ Guardar último frame visible
        image.close()

        // Detectar sobre imagen no-espejada
        val detections = faceDetector.detect(frame)
        if (detections.isEmpty()) {
            Log.v("FaceSdk", "⚠️ Ningún rostro detectado en frame ${frame.width}x${frame.height}")
            return@withContext emptyList()
        }

        val results = mutableListOf<FaceSdkResult>()

        for (d in detections) {
            val bboxReal = Rect(
                d.box.left.toInt(),
                d.box.top.toInt(),
                d.box.right.toInt(),
                d.box.bottom.toInt()
            )

            // Calcular bbox para overlay si la cámara es frontal
            val bboxOverlay = if (isFrontCamera) {
                Rect(
                    frame.width - bboxReal.right,
                    bboxReal.top,
                    frame.width - bboxReal.left,
                    bboxReal.bottom
                )
            } else bboxReal

            var spoof: SpoofResult? = null
            var embedding: FloatArray? = null

            // ============================================================
            // 🧩 FACE EMBEDDING
            // ============================================================

            if (doSpoof) {
                try {
                    Log.d("FaceSdk", "🧠 Ejecutando SpoofDetector en bbox=$bboxReal")
                    // El detector ya realiza:
                    //  - cropByScale(2.7f / 4.0f)
                    //  - conversión RGB→BGR
                    //  - sin normalización (usa 0..255)
                    //  - promedio de ambos modelos
                    spoof = spoofDetector.detect(frame, bboxReal)
                    Log.d(
                        "FaceSdk",
                        "✅ SpoofDetector result: isLive=${spoof?.isLive} | liveScore=${spoof?.liveScore?.format(3)}"
                    )
                } catch (e: Exception) {
                    Log.e("FaceSdk", "❌ Error en spoof detector: ${e.message}")
                }
            }

            // ============================================================
            // 🧩 FACE EMBEDDING
            // ============================================================
            if (doEmbedding) {
                try {
                    // 1) Recorte cuadrado con margen (15%)
                    val crop = safeCropWithMargin(frame, bboxReal, 0.15f)
                    // 2) Resize + normalización dentro de FaceNetEngine
                    embedding = crop?.let { faceNetEngine.getEmbedding(it) }
                    Log.d(
                        "FaceSdk",
                        "✅ Embedding generado (${embedding?.size ?: 0} valores)"
                    )
                } catch (e: Exception) {
                    Log.e("FaceSdk", "❌ Error en embedding: ${e.message}")
                }
            }

            // ============================================================
            // 🧩 Resultado unificado del SDK
            // ============================================================
            results += FaceSdkResult(
                name = null,
                similarity = null,
                bbox = bboxOverlay,
                spoof = spoof,
                embedding = embedding
            )
        }

        results
    }

    // ============================================================
    //  PROCESS PHOTO — Flujo de foto HD (registro o verificación)
    // ============================================================
    suspend fun processPhoto(bitmap: Bitmap): FaceSdkResult? = withContext(Dispatchers.Default) {
        try {
            // 1️⃣ Detección de rostro (MediaPipe exige ARGB_8888)
            val detections = faceDetector.detect(bitmap)
            val face = detections.firstOrNull() ?: run {
                Log.w("FaceSdk", "⚠️ No se detectó rostro en foto HD")
                return@withContext null
            }

            // 2️⃣ Bounding box exacto del rostro detectado
            val bbox = Rect(
                face.box.left.toInt(),
                face.box.top.toInt(),
                face.box.right.toInt(),
                face.box.bottom.toInt()
            )

            // 3️⃣ Recorte del rostro con margen
            val cropped = safeCropWithMargin(bitmap, bbox, 0.15f)
            if (cropped == null) {
                Log.w("FaceSdk", "⚠️ Error al recortar rostro HD")
                return@withContext null
            }

            // 4️⃣ Preparar imagen para FaceNet (limpiar canal alpha)
            val prepared = cropped.prepareForEmbedding()

            // 5️⃣ Generar embedding 512D con pipeline estándar
            val embedding = faceNetEngine.getEmbedding(prepared)

            Log.d("FaceSdk", "✅ Embedding generado (${embedding.size} valores)")

            // 6️⃣ Empaquetar resultado
            FaceSdkResult(
                name = null,
                similarity = null,
                bbox = bbox,
                spoof = null,
                embedding = embedding
            )

        } catch (e: Exception) {
            Log.e("FaceSdk", "❌ Error en processPhoto: ${e.message}", e)
            null
        }
    }


    // ============================================================
    //  IDENTIFY — Comparar embeddings (1:N)
    // ============================================================
    suspend fun identify(
        liveEmbedding: FloatArray,
        databaseEmbeddings: List<FloatArray>,
        threshold: Float = 0.75f
    ): CosineResult = withContext(Dispatchers.Default) {

        var bestScore = -1f
        var bestIndex: Int? = null

        for (i in databaseEmbeddings.indices) {
            val score = cosineSimilarity(liveEmbedding, databaseEmbeddings[i])
            if (score > bestScore) {
                bestScore = score
                bestIndex = i
            }
        }

        CosineResult(
            matched = bestScore >= threshold,
            bestIndex = bestIndex,
            score = bestScore
        )
    }

    /**
     * Genera un embedding facial (vector de 512 dimensiones) a partir de un rostro ya detectado.
     *
     * 🧩 Esta función es útil cuando ya tienes una detección previa (por ejemplo, desde analyzeFrame)
     * y deseas generar el embedding sin volver a ejecutar el detector de rostros.
     *
     * ✅ Se recomienda usar esta función cuando:
     * - Ya tienes un bounding box válido del rostro (bboxMirror).
     * - Quieres acelerar el proceso evitando redetectar en la imagen HD o frame actual.
     * - Deseas comparar directamente contra embeddings almacenados (verificación o autenticación rápida).
     *
     * ⚠️ Importante:
     * - El parámetro bboxMirror debe corresponder a un rostro detectado en una imagen espejada
     *   (como la cámara frontal). La función lo "desespeja" internamente para mantener la geometría correcta.
     * - El frameBitmap debe tener formato ARGB_8888 (como los que entrega CameraXPreviewAnalyzer).
     * - El método safeCropWithMargin() aplica un margen del 15% para capturar mejor la cara completa.
     * - Se usa prepareForEmbedding() para limpiar el canal alpha antes de pasar a FaceNet.
     *
     * @param frameBitmap  Bitmap original del frame (sin recortes).
     * @param bboxMirror   Bounding box del rostro en coordenadas espejadas (de la cámara frontal).
     * @return Un arreglo FloatArray con 512 valores normalizados (L2), o un vector nulo si falla.
     */
    suspend fun getEmbedding(frameBitmap: Bitmap, bboxMirror: Rect): FloatArray =
        withContext(Dispatchers.Default) {
            try {
                // 1️⃣ Convertir de coordenadas espejo a reales
                val nmRect = mirroredToNonMirrored(bboxMirror, frameBitmap.width)

                // 2️⃣ Recortar rostro con margen seguro
                val crop = safeCropWithMargin(frameBitmap, nmRect, 0.15f)
                if (crop == null) {
                    Log.w("FaceSdk", "⚠️ No se pudo recortar rostro válido")
                    return@withContext FloatArray(faceNetEngine.embeddingDim) { 0f }
                }

                // 3️⃣ Preparar para FaceNet (RGB limpio)
                val prepared = crop.prepareForEmbedding()

                // 4️⃣ Generar embedding
                val emb = faceNetEngine.getEmbedding(prepared)
                Log.d("FaceSdk", "✅ Embedding generado desde frame (${emb.size} valores)")
                emb

            } catch (e: Exception) {
                Log.e("FaceSdk", "❌ Error en getEmbedding: ${e.message}", e)
                FloatArray(faceNetEngine.embeddingDim) { 0f }
            }
        }

    /**
     * Spoof directo para foto/bitmap: UI pasa frame (no espejado) + bbox ESPEJO.
     * El SDK convierte bbox → no-espejo y evalúa sobre el frame NO-espejo.
     */

    fun close() {


        try {
            faceDetector.close() // 🔹 BlazeFace (detección)
        } catch (_: Exception) {
            Log.w("FaceSdk", "⚠️ Error cerrando FaceDetector")
        }

        try {
            faceNetEngine.close() // 🔹 FaceNet (embeddings)
        } catch (_: Exception) {
            Log.w("FaceSdk", "⚠️ Error cerrando FaceNetEngine")
        }

        try {
            spoofDetector.close() // 🔹 Spoof detector (liveness)
        } catch (_: Exception) {
            Log.w("FaceSdk", "⚠️ Error cerrando SpoofDetector")
        }

        // 🔹 Limpieza general
        lastFrameBitmap = null

        Log.d("FaceSdk", "🧹 SDK cerrado y liberado correctamente")
    }
}