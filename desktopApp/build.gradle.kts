import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        named("desktopMain") {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.piliplus.recodeing.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "liquidreode"
            packageVersion = "0.1.0"
            vendor = "SXD"
            modules("java.desktop", "java.logging", "java.naming", "java.net.http", "java.prefs", "java.sql")
            windows {
                menuGroup = "liquidreode"
                upgradeUuid = "B8B21B6A-0AD8-44A7-9016-A9256ED02652"
            }
        }
    }
}
