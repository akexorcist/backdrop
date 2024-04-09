import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.akexorcist.backdrop"
version = "1.0.1"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs)
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("com.github.sarxos:webcam-capture:0.3.12")

    // Until they accept my pull request
//    implementation("com.github.eduramiba:webcam-capture-driver-native:master-SNAPSHOT")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.fasterxml:aalto-xml:1.3.2")
    implementation("org.openjfx:javafx-graphics:19")
}

compose.desktop {
    application {
        mainClass = "com.akexorcist.backdrop.ui.AppKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
            )

            macOS {
                bundleID = "com.akexorcist.backdrop"
                iconFile.set(project.file("icon.icns"))
                entitlementsFile.set(project.file("macos/entitlement.plist"))
                infoPlist {
                    extraKeysRawXml = project.file("macos/info.plist").readText()
                }
            }

            packageName = "Backdrop"
            packageVersion = "1.0.1"
            description = "Video and audio projection app for your streaming content"
            licenseFile.set(project.file("LICENSE.txt"))
        }

        buildTypes {
            release {
                proguard {
                    configurationFiles.from("proguard-rules.pro")
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

javafx {
    version = "20"
    modules("javafx.media")
}
