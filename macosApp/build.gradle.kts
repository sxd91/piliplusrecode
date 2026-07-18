plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "com.piliplus.recodeing.main"
            baseName = "PiliPlusRecodeing"
            binaryOption("smallBinary", "true")
        }
    }

    sourceSets {
        macosArm64Main.dependencies {
            implementation(project(":shared"))
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }
}
