import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.akexorcist.screensharing"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("com.github.sarxos:webcam-capture:0.3.12")
//    implementation("com.github.eduramiba:webcam-capture-driver-native:1.0.0-SNAPSHOT")
    implementation("com.github.eduramiba:webcam-capture-driver-native:master-SNAPSHOT")
    implementation("io.insert-koin:koin-core:3.4.3")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
//                TargetFormat.Msi,
//                TargetFormat.Deb,
            )
            packageName = "ScreenSharing"
            packageVersion = "1.0.0"
        }
    }
}

javafx {
    version = "20"
    modules("javafx.media")
}
