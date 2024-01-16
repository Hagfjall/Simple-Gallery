import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

val isProprietary = gradle.startParameter.taskNames.any { task -> task.contains("Proprietary") }

plugins {
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.imgly).apply(false)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {

    compileSdkVersion(project.libs.versions.app.build.compileSDKVersion.get().toInt())
    defaultConfig {
        applicationId = libs.versions.app.version.appId.get()
        minSdk = project.libs.versions.app.build.minimumSDK.get().toInt()
        targetSdk = project.libs.versions.app.build.targetSDK.get().toInt()
        versionName = project.libs.versions.app.version.versionName.get()
        versionCode = project.libs.versions.app.version.versionCode.get().toInt()
        setProperty("archivesBaseName", "gallery-$versionCode")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            register("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            // we cannot change the original package name, else PhotoEditorSDK won't work
            //applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions.add("licensing")
    productFlavors {
        register("proprietary")
        register("foss")
        register("prepaid")
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        if (isProprietary) {
            getByName("main").java.srcDirs("src/proprietary/kotlin")
        }
    }

    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get().toString())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

//    kotlin {
//        jvmToolchain(1.8)
//    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = project.libs.versions.app.build.kotlinJVMTarget.get()
    }

    namespace = libs.versions.app.version.appId.get()

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "META-INF/library_release.kotlin_module"
        }
    }
}

dependencies {
    implementation(libs.android.gif.drawable)
    implementation(libs.android.image.cropper)
    implementation(libs.androidsvg.aar)
    implementation(libs.androidx.composeruntime)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.apng)
    implementation(libs.awebp)
    implementation(libs.exif)
    implementation(libs.gestureviews)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.imagefilters)
    implementation(libs.okio)
    implementation(libs.sanselan)
    implementation(libs.sdk.panowidget)
    implementation(libs.sdk.videowidget)
    implementation(libs.simple.tools.commons)
    implementation(libs.subsamplingscaleimageview)
    implementation(libs.picasso) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    compileOnly(libs.okhttp)

    ksp(libs.glide.compiler)
    implementation(libs.zjupure.webpdecoder)

    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}
// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}

// Apply the PESDKPlugin
if (isProprietary) {
    apply(from = "../gradle/imglysdk.gradle")
}
