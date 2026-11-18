import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun cfg(name: String): String = (localProps.getProperty(name) ?: System.getenv(name) ?: "").trim()
fun quoted(name: String) = "\"${cfg(name).replace("\"", "")}\""

android {
    namespace = "ng.lightgrab.courier"
    compileSdk = 35

    defaultConfig {
        applicationId = "ng.lightgrab.courier"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Only publishable values. Service-role keys must never be placed here.
        buildConfigField("String", "SUPABASE_URL", quoted("SUPABASE_URL"))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", quoted("SUPABASE_PUBLISHABLE_KEY"))
        buildConfigField("String", "FIREBASE_APP_ID", quoted("FIREBASE_APP_ID"))
        buildConfigField("String", "FIREBASE_API_KEY", quoted("FIREBASE_API_KEY"))
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted("FIREBASE_PROJECT_ID"))
        buildConfigField("String", "FIREBASE_SENDER_ID", quoted("FIREBASE_SENDER_ID"))
        buildConfigField("boolean", "EARNINGS_ENABLED", "false")
    }

    signingConfigs {
        create("release") {
            if (cfg("RELEASE_STORE_FILE").isNotEmpty()) {
                storeFile = file(cfg("RELEASE_STORE_FILE"))
                storePassword = cfg("RELEASE_STORE_PASSWORD")
                keyAlias = cfg("RELEASE_KEY_ALIAS")
                keyPassword = cfg("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (cfg("RELEASE_STORE_FILE").isNotEmpty()) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/INDEX.LIST", "/META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.exifinterface)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.livekit.android)
    implementation(libs.osmdroid)
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
}
