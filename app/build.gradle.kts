plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.test.galaxyUP"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.test.galaxyUP"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Cambiado a 1.8 para máxima compatibilidad
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // ---------- DEPENDENCIAS BÁSICAS Y DE UI (XML) ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // ---------- DEPENDENCIAS DE RED Y TAREAS ASÍNCRONAS ----------
    // Retrofit (para hacer las llamadas a la API)
    implementation(libs.retrofit)
    // Gson Converter (para convertir JSON a objetos Kotlin)
    implementation(libs.converter.gson)
    // Logging Interceptor (para ver las llamadas de red en Logcat)
    implementation(libs.logging.interceptor)
    // Coroutines (para manejar las llamadas de red en segundo plano)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    // Lifecycle (necesario para lifecycleScope)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    // ---------- DEPENDENCIAS DE TEST ----------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Las siguientes dependencias son de Compose y Firebase, se mantienen por si las usas en otro lado,
    // pero no son estrictamente necesarias para la lógica actual.
    // implementation(libs.androidx.activity.compose)
    // implementation(platform(libs.androidx.compose.bom))
    // implementation(libs.androidx.ui)
    // ... etc ...
    // implementation(libs.firebase.annotations)
}