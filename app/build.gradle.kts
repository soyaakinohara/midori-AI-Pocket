plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // ← これを追加
}

android {
    namespace = "com.soyaakinohara.aipocket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.soyaakinohara.aipocket"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets",
                    "src\\main\\assets",
                    "src\\main\\assets"
                )
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // ... 他の依存関係はそのまま ...

    // Navigation Compose
    val nav_version = "2.7.7" // 2024年3月時点での比較的新しいバージョン例
    implementation("androidx.navigation:navigation-compose:$2.7.7")

    // あとでViewModelも使うので、先に追加しておいても良いでしょう
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // 2024年3月時点でのバージョン例

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.6") // 2024年3月時点での安定版

    val room_version = "2.7.1" // 2024年3月時点での安定版

    implementation("androidx.room:room-runtime:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1") // Kotlinの場合はこれ (重要)
    implementation("androidx.room:room-ktx:2.7.1")

    // Kotlin拡張機能とコルーチンサポート (推奨)
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("io.coil-kt:coil-compose:2.6.0") // 2024年3月時点でのバージョン例

    implementation("androidx.compose.material:material-icons-extended:1.6.4")

    implementation("androidx.activity:activity-compose:1.8.2") // 2024年3月時点での安定版例

    //implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.0.0")

// オプション - RxJava2/3 サポート (今回は使わないが参考までに)
// implementation("androidx.room:room-rxjava2:$room_version")
// implementation("androidx.room:room-rxjava3:$room_version")

// オプション - Paging 3 サポート (今回は使わないが参考までに)
// implementation("androidx.room:room-paging:$room_version")
// モジュールレベルの build.gradle (Module :app)
    //constraints {
        //implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.0.0") { // 上のimplementationと同じバージョン
            //because("Ensuring Kotlin metadata compatibility for Room with Kotlin 2.0")
       // }
   // }
    //// ↑↑↑ constraintsブロックの終了 ↑↑↑
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.0"))

    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
}