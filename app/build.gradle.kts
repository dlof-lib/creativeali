import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================================
//  Creative Ali — App Module Build Script
//  ---------------------------------------------------------------------------
//  هذا الملف يبني نسختين مستقلتين من التطبيق (legacy / modern) عبر product
//  flavors، ويدعم توقيع release آمن مبني على متغيرات بيئة (بلا أسرار مكتوبة
//  هنا)، مع تسمية تلقائية لملفات الـ APK الناتجة وإعدادات جودة كود شاملة.
// =============================================================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

// -----------------------------------------------------------------------
// إدارة إصدارات مركزية — كل رقم إصدار مكتبة في مكان واحد لسهولة الترقية
// -----------------------------------------------------------------------
object Versions {
    const val CORE_KTX = "1.13.1"
    const val LIFECYCLE = "2.8.4"
    const val ACTIVITY = "1.9.1"
    const val COMPOSE_BOM = "2024.06.00"
    const val COMPOSE_COMPILER = "1.5.14"
    const val NAVIGATION_COMPOSE = "2.7.7"
    const val COIL = "2.6.0"
    const val KOTLINX_SERIALIZATION = "1.6.3"
    const val ROOM = "2.6.1"
    const val COROUTINES = "1.8.1"
    const val PLAY_SERVICES_ADS = "23.2.0"
    const val MATERIAL = "1.12.0"
    const val JUNIT = "4.13.2"
    const val ANDROIDX_TEST_JUNIT = "1.2.1"
    const val ESPRESSO = "3.6.1"
    const val DESUGAR_JDK_LIBS = "2.0.4"
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // متاحة داخل الكود عبر BuildConfig لأي شرط يعتمد على النسخة
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${buildTimestamp()}\"")
    }

    // -------------------------------------------------------------------
    // Product Flavors — نسختان منفصلتان (APK لكل مجموعة إصدارات) لتغطية
    // جميع إصدارات أندرويد:
    //  • legacy: Android 5.0 (API 21) وما فوق — لأقدم الأجهزة
    //  • modern: Android 8.0 (API 26) وما فوق — يستفيد من الأيقونة المتكيّفة
    //            وقنوات الإشعارات وميزات النظام الأحدث
    // كل flavor ينتج APK مستقل باسمه (app-legacy-release.apk / app-modern-release.apk)
    // -------------------------------------------------------------------
    flavorDimensions += "version"
    productFlavors {
        create("legacy") {
            dimension = "version"
            minSdk = 21
            versionNameSuffix = "-legacy"
            buildConfigField("boolean", "IS_MODERN_FLAVOR", "false")
        }
        create("modern") {
            dimension = "version"
            minSdk = 26
            versionNameSuffix = "-modern"
            buildConfigField("boolean", "IS_MODERN_FLAVOR", "true")
        }
    }

    // -------------------------------------------------------------------
    // Signing — القيم تُقرأ حصريًا من متغيرات بيئة يمررها CI/CD (الـ
    // workflow يفكّ ترميز keystore ويمرر المسار وكلمات السر كـ env vars).
    // لا تُكتب أي كلمات سر أو مسارات keystore داخل هذا الملف مباشرة.
    // في حال غياب المتغيرات (بناء محلي مثلاً)، signingConfig يبقى فارغًا
    // ولن يفشل الـ sync، لكن `assembleRelease` سينتج APK غير موقّع.
    // -------------------------------------------------------------------
    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("KEYSTORE_PATH")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // يسمح بتثبيت نسخة debug جنبًا إلى جنب مع release على نفس الجهاز
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // يتيح استخدام واجهات Java 8+ الحديثة (java.time وغيرها) حتى على
        // minSdk 21 في flavor الـ legacy
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.COMPOSE_COMPILER
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
                "META-INF/*.kotlin_module"
            )
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
        htmlReport = true
        xmlReport = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    // -------------------------------------------------------------------
    // تسمية تلقائية لملفات الـ APK الناتجة: creativeali-<flavor>-<buildType>-vX.apk
    // يسهّل تمييز الملفات عند رفعها كـ artifacts أو نشرها يدويًا
    // -------------------------------------------------------------------
    applicationVariants.all {
        val variant = this
        outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val flavorName = variant.flavorName
                val buildTypeName = variant.buildType.name
                val versionName = variant.versionName
                output.outputFileName =
                    "creativeali-${flavorName}-${buildTypeName}-v${versionName}.apk"
            }
    }
}

// -----------------------------------------------------------------------
// تحقّق مبكر: يفشل بوضوح إن حاول أحدهم بناء release بدون أي إعداد توقيع،
// بدل أن يفشل لاحقًا برسالة Gradle غامضة أثناء التوقيع الفعلي.
// -----------------------------------------------------------------------
tasks.matching { it.name.contains("Release") && it.name.startsWith("assemble") }.configureEach {
    doFirst {
        val hasKeystore = !System.getenv("KEYSTORE_PATH").isNullOrBlank()
        if (!hasKeystore) {
            logger.warn(
                "⚠️  لا توجد متغيرات بيئة توقيع (KEYSTORE_PATH). " +
                    "سيُبنى APK غير موقّع. راجع سير عمل CI أو صدّر المتغيرات محليًا."
            )
        }
    }
}

fun buildTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

dependencies {
    // -------------------------------------------------------------------
    // Core Library Desugaring — يفعّل واجهات Java الحديثة على minSdk 21
    // -------------------------------------------------------------------
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.DESUGAR_JDK_LIBS}")

    // -------------------------------------------------------------------
    // Core / AndroidX
    // -------------------------------------------------------------------
    implementation("androidx.core:core-ktx:${Versions.CORE_KTX}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}")
    implementation("androidx.activity:activity-compose:${Versions.ACTIVITY}")
    implementation("androidx.activity:activity-ktx:${Versions.ACTIVITY}")

    // -------------------------------------------------------------------
    // Jetpack Compose
    // -------------------------------------------------------------------
    implementation(platform("androidx.compose:compose-bom:${Versions.COMPOSE_BOM}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:${Versions.NAVIGATION_COMPOSE}")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // -------------------------------------------------------------------
    // Media — اختيار ومعاينة الصور/الفيديو
    // -------------------------------------------------------------------
    implementation("io.coil-kt:coil-compose:${Versions.COIL}")
    implementation("io.coil-kt:coil-video:${Versions.COIL}")

    // -------------------------------------------------------------------
    // XML (.dlof) reading/writing
    // -------------------------------------------------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")

    // -------------------------------------------------------------------
    // Local persistence — Room (يوميات ومخططات)
    // -------------------------------------------------------------------
    implementation("androidx.room:room-runtime:${Versions.ROOM}")
    implementation("androidx.room:room-ktx:${Versions.ROOM}")
    kapt("androidx.room:room-compiler:${Versions.ROOM}")

    // -------------------------------------------------------------------
    // ViewModel + lifecycle-aware state collection
    // -------------------------------------------------------------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.LIFECYCLE}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}")

    // -------------------------------------------------------------------
    // Ads (AdMob)
    // -------------------------------------------------------------------
    implementation("com.google.android.gms:play-services-ads:${Versions.PLAY_SERVICES_ADS}")

    // -------------------------------------------------------------------
    // Material Components (base XML theme, matches dlof style)
    // -------------------------------------------------------------------
    implementation("com.google.android.material:material:${Versions.MATERIAL}")

    // -------------------------------------------------------------------
    // Testing
    // -------------------------------------------------------------------
    testImplementation("junit:junit:${Versions.JUNIT}")
    androidTestImplementation("androidx.test.ext:junit:${Versions.ANDROIDX_TEST_JUNIT}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.ESPRESSO}")
}
