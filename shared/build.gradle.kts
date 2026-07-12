import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library")
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "mx.utng.mamr.smarthealthmonitor.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        buildConfigField("String", "MQTT_BROKER_URL", "\"${properties.getProperty("mqtt.broker_url") ?: ""}\"")
        buildConfigField("String", "MQTT_USERNAME", "\"${properties.getProperty("mqtt.username") ?: ""}\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"${properties.getProperty("mqtt.password") ?: ""}\"")
        buildConfigField("String", "NEON_API_KEY", "\"${properties.getProperty("NEON_API_KEY") ?: ""}\"")
        buildConfigField("String", "NEON_HOST", "\"${properties.getProperty("NEON_HOST") ?: ""}\"")
        buildConfigField("String", "NEON_DB", "\"${properties.getProperty("NEON_DB") ?: ""}\"")
        buildConfigField("String", "NEON_USER", "\"${properties.getProperty("NEON_USER") ?: ""}\"")
        buildConfigField("String", "NEON_PASSWORD", "\"${properties.getProperty("NEON_PASSWORD") ?: ""}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Room
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    implementation(libs.kotlinx.coroutines.play.services)

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Retrofit + OkHttp para llamadas a Neon HTTP API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
