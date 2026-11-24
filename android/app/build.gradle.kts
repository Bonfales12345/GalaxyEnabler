plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.bonfales.galaxyenabler"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.bonfales.galaxyenabler"
        minSdk = 36
        targetSdk = 36
        versionCode = 6
        versionName = "3.0-GalaxyEnabler"
    }

    splits {
        abi {
            isEnable = false
        }
    }

    signingConfigs {
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
        val customKeystore = rootProject.file("debug.keystore")
        if (customKeystore.exists()) {
            create("packKey") {
                storeFile = customKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    val activeSigning = signingConfigs.findByName("packKey") ?: signingConfigs.getByName("debug")

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = activeSigning
        }
        debug {
            signingConfig = activeSigning
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3:1.5.0-alpha29")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.graphics:graphics-shapes:1.0.1")
}
