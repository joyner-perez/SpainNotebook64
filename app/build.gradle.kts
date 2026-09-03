plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.joyner.notebook64"
    compileSdk {
        version =
            release(37) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "com.joyner.notebook64"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    flavorDimensions += listOf("services")
    productFlavors {
        create("yesGoogleServices") {
            dimension = "services"
            isDefault = true
        }
        create("noGoogleServices") {
            dimension = "services"
        }
    }
}

dependencies {
    implementation(project(":notebook64"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.accompanist.permissions)
    "yesGoogleServicesImplementation"(libs.quickie.unbundled)
    "noGoogleServicesImplementation"(libs.quickie.bundled)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
