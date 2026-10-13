plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "org.nexlibre.orbita"
    compileSdk = 35
    defaultConfig {
        applicationId = "org.nexlibre.orbita"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.2.0-alpha.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        val path = System.getenv("ORBITA_SIGNING_PATH")
        val password = System.getenv("ORBITA_SIGNING_PASSWORD")
        if (!path.isNullOrBlank() && !password.isNullOrBlank() && file(path).isFile) {
            create("persistentDevelopment") {
                storeFile = file(path); storePassword = password
                keyAlias = "orbita-development"; keyPassword = password
            }
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfigs.findByName("persistentDevelopment")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.03.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
