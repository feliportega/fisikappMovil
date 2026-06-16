plugins {
    alias(libs.plugins.android.application)
}

val unityStreamingAssetsValue =
    (findProperty("unityStreamingAssets") as? String).orEmpty()

val unityNoCompressExtensions =
    listOf(
        ".unity3d",
        ".ress",
        ".resource",
        ".obb",
        ".bundle",
        ".unityexp"
    ) + unityStreamingAssetsValue
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

android {
    namespace = "com.marcos.fisikappmovil"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marcos.fisikappmovil"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        ignoreAssetsPattern =
            "!.svn:!.git:!.ds_store:!*.scc:!CVS:!thumbs.db:!picasa.ini:!*~"

        noCompress += unityNoCompressExtensions
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

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit
    implementation(libs.com.google.code.gson.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation("com.google.android.material:material:1.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(project(":facesdk"))
    implementation(project(":unityLibrary"))
    implementation("androidx.appcompat:appcompat:1.6.1")


    // CameraX
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("androidx.camera:camera-extensions:1.5.1")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}