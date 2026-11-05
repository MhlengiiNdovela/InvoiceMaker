import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.invoicemaker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // TODO: Move these to local.properties / BuildConfig for real projects.
        // Never commit real API keys to GitHub - use secrets/gradle.properties + .gitignore.
        buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"5ee112bd53b8f6482b2dc0365ee112bd53b8f6482b2dc036\"")
        buildConfigField("String", "SENDGRID_API_KEY", "\"6K833T82MA84C8KSNKREAE1N\"")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.android.material:material:1.14.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")

    // Retrofit + OkHttp + Gson (Supabase, exchange rates, email)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
