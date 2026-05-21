package com.marcos.fisikappmovil.facenet

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.dcl.facesdk.FaceSdk
import com.dcl.facesdk.FaceSdkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object FaceSdkBridge {

    interface AnalyzeSuccessCallback {
        fun onSuccess(results: List<FaceSdkResult>)
    }

    interface ErrorCallback {
        fun onError(error: Throwable)
    }

    interface PhotoSuccessCallback {
        fun onSuccess(result: FaceSdkResult?)
    }

    @JvmStatic
    fun analyzeFrameAsync(
        sdk: FaceSdk,
        image: ImageProxy,
        isFrontCamera: Boolean,
        doSpoof: Boolean,
        doEmbedding: Boolean,
        onSuccess: AnalyzeSuccessCallback,
        onError: ErrorCallback
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val result = sdk.analyzeFrame(image, isFrontCamera, doSpoof, doEmbedding)
                onSuccess.onSuccess(result)
            } catch (e: Throwable) {
                onError.onError(e)
            }
        }
    }

    @JvmStatic
    fun processPhotoBlocking(
        sdk: FaceSdk,
        bitmap: Bitmap
    ): FaceSdkResult? = runBlocking {
        sdk.processPhoto(bitmap)
    }

    @JvmStatic
    fun processPhotoAsync(
        sdk: FaceSdk,
        bitmap: Bitmap,
        onSuccess: PhotoSuccessCallback,
        onError: ErrorCallback
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val result = sdk.processPhoto(bitmap)
                onSuccess.onSuccess(result)
            } catch (e: Throwable) {
                onError.onError(e)
            }
        }
    }
}