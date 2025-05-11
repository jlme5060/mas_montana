plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mas_montaa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mas_montaa"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX core + Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)

    // ✅ AndroidSVG (para cargar SVG desde res/raw)
    implementation("com.caverock:androidsvg:1.4")

    // 🗺️ Mapsforge (mapas offline)
    implementation("org.mapsforge:mapsforge-map-android:0.16.0")
    implementation("org.mapsforge:mapsforge-map:0.16.0")
    implementation("org.mapsforge:mapsforge-core:0.16.0")
    implementation("org.mapsforge:mapsforge-themes:0.16.0")

    // 🌍 Osmdroid (mapas online tipo OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:6.1.11")
    implementation(libs.androidx.storage)
    implementation("androidx.constraintlayout:constraintlayout:2.2.0") // La versión más reciente puede variar, consulta la documentación oficial

    // 🧪 Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // 🐞 Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
