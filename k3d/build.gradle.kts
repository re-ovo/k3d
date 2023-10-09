import com.vanniktech.maven.publish.SonatypeHost
import kotlin.math.sign

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "me.rerere.k3d"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    coordinates("me.rerere", "k3d", "1.0.0-SNAPSHOT")
    pom {
        name.set("K3D")
        description.set("Android 3D library")
        inceptionYear.set("2023")
        url.set("https://github.com/re-ovo/k3d")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("re_ovo")
                name.set("RE_OVO")
                email.set("re_dev@qq.com")
                url.set("https://github.com/re-ovo")
            }
        }
        scm {
            url.set("https://github.com/re-ovo/k3d/")
            connection.set("scm:git:git://github.com/re-ovo/k3d.git")
            developerConnection.set("scm:git:ssh://git@github.com/re-ovo/k3d.git")
        }
    }
}