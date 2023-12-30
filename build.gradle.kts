import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.arnyminerz"
version = project.properties["weewx-fixer.version"]!!

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

tasks.withType(KotlinCompilationTask::class.java) {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation("com.jcraft:jsch:0.1.55")
                implementation("com.darkrockstudios:mpfilepicker:3.1.0")
                implementation("org.json:json:20231013")
                implementation("com.vdurmont:semver4j:3.1.0")
                implementation("org.jsoup:jsoup:1.17.2")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WeeWX Fixer"
            packageVersion = project.properties["weewx-fixer.version"] as String?
            windows {
                iconFile.set(project.file("src/jvmMain/resources/Weewx.ico"))
                dirChooser = true
                perUserInstall = true
                menuGroup = "Weather"
                upgradeUuid = "cbc23044-85cc-4ef7-b52f-515f5704955f" // https://www.guidgen.com/
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/Weewx.png"))
                debMaintainer = "arnyminerz@proton.me"
                menuGroup = "weather"
                appRelease = "1"
                appCategory = "tools"
                rpmLicenseType = "Apache-2.0"
            }
        }
    }
}
