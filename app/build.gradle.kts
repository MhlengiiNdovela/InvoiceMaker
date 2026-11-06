import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun configValue(name: String): String {
    val value = localProperties.getProperty(name) ?: System.getenv(name) ?: ""
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

android {
    namespace = "com.example.invoicemaker"
    compileSdk = 36   // 37 is very new; go back to 37 once everything builds

    defaultConfig {
        applicationId = "com.example.invoicemaker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "EXCHANGE_RATE_API_KEY", configValue("EXCHANGE_RATE_API_KEY"))
        buildConfigField("String", "SENDGRID_API_KEY", configValue("SENDGRID_API_KEY"))
        buildConfigField("String", "SUPABASE_URL", configValue("SUPABASE_URL"))
        buildConfigField("String", "SUPABASE_ANON_KEY", configValue("SUPABASE_ANON_KEY"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}   // <-- closes android { }

dependencies {
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.compose.runtime:runtime:1.12.1")
    implementation("com.google.android.material:material:1.14.0")
    // ...keep your dependencies exactly as they are...
}
// no extra } here