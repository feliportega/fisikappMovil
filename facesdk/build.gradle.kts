plugins {
    id("com.android.library")
}

android {
    namespace = "com.dcl.facesdk"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        namespace = "com.dcl.facesdk"

        compileSdk = 36

        defaultConfig {
            minSdk = 24
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX
    implementation("androidx.camera:camera-core:1.5.1")

    // MediaPipe Tasks – Face Detection
    implementation("com.google.mediapipe:tasks-vision:0.20230731")

    /*
        LiteRT 1.4.0 (equivalente TFLite 1.4.0)
        TensorFlow Lite dependencies
     */
    implementation("com.google.ai.edge.litert:litert:1.4.0")
    implementation("com.google.ai.edge.litert:litert-support:1.4.0")
    implementation("com.google.ai.edge.litert:litert-gpu:1.4.0")
    implementation("com.google.ai.edge.litert:litert-gpu-api:1.4.0")

    // ML KIT
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")


    // Exif para corrección de rotación si alimentas por URI o JPEG
    implementation("androidx.exifinterface:exifinterface:1.4.1")
}