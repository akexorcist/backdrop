import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.akexorcist.backdrop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.github.sarxos:webcam-capture:0.3.12")
    implementation("com.github.eduramiba:webcam-capture-driver-native:master-SNAPSHOT")
    implementation("io.insert-koin:koin-core:3.5.3")
}

compose.desktop {
    application {
        mainClass = "com.akexorcist.backdrop.ui.AppKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
//                TargetFormat.Msi,
            )

            macOS {
                bundleID = "com.akexorcist.backdrop"
                iconFile.set(project.file("image/icons/icon.icns"))
                entitlementsFile.set(project.file("macos/entitlement.plist"))
                infoPlist {
                    extraKeysRawXml = project.file("macos/info.plist").readText()
                }
            }

            windows {
                iconFile.set(project.file("image/icons/icon.ico"))
            }

            packageName = "Backdrop"
            packageVersion = "1.0.0"
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
