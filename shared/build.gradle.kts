import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "com.piliplus.recodeing.shared"
        compileSdk = 37
        minSdk = 23
        androidResources.enable = true
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    jvm("desktop")
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.miuix.ui)
            implementation(libs.miuix.preference)
            implementation(libs.miuix.icons)
            implementation(libs.backdrop)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        val skikoMain by creating {
            dependsOn(commonMain.get())
        }
        getByName("desktopMain").apply {
            dependsOn(skikoMain)
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        macosArm64Main.get().apply {
            dependsOn(skikoMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosMain by creating {
            dependsOn(skikoMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)
    }
}
