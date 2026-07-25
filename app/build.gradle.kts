plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.creativeali.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.creativeali.app"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    // نسختان منفصلتان (APK لكل مجموعة إصدارات) لتغطية جميع إصدارات أندرويد:
    //  - legacy: Android 5.0 (API 21) وما فوق — لأقدم الأجهزة
    //  - modern: Android 8.0 (API 26) وما فوق — يستفيد من الأيقونة المتكيّفة وقنوات الإشعارات وغيرها
    // كل flavor بينتج ملف APK مستقل باسمه (مثال: app-legacy-release.apk / app-modern-release.apk)
    flavorDimensions += "version"
    productFlavors {
        create("legacy") {
            dimension = "version"
            minSdk = 21
            versionNameSuffix = "-legacy"
        }
        create("modern") {
            dimension = "version"
            minSdk = 26
            versionNameSuffix = "-modern"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // Media (image/video pick + preview)
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")

    // XML (.dlof) reading/writing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Local persistence for journal entries and diagrams
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ViewModel + lifecycle-aware state collection
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Ads (AdMob)
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // Material Components (base XML theme, matches dlof style)
    implementation("com.google.android.material:material:1.12.0")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
}
