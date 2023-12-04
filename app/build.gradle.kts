plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.StressOverflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.StressOverflow"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res\\layouts\\Item",
                    "src\\main\\res\\layouts\\Tag",
                    "src\\main\\res\\layouts\\Image",
                    "src\\main\\res\\layouts\\SignIn",
                    "src\\main\\res\\layouts\\misc",
                    "src\\main\\res\\layouts",
                    "src\\main\\res"
                )
            }
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.mockito:mockito-core:2.25.0")
    androidTestImplementation("org.mockito:mockito-inline:2.13.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.loopj.android:android-async-http:1.4.10")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")

}