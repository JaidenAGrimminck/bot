plugins {
    id("java")
}

group = "me.autobot"
version = "1.0-SNAPSHOT"

// buildscript {
//     repositories {
//         jcenter()
//         mavenCentral()
//         maven {
//             url = uri("https://plugins.gradle.org/m2/")
//         }
//     }
//     // dependencies {
//     //     classpath("gradle.plugin.org.ros2.tools.gradle:ament:0.7.0")
//     // }
// }

repositories {
    mavenCentral()
}

// ament {
//     entryPoints {
//         consoleScripts = []
//     }
// }

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.pi4j:pi4j-ktx:2.4.0") // Kotlin DSL
    implementation("com.pi4j:pi4j-core:2.3.0")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:2.3.0")
    implementation("com.pi4j:pi4j-plugin-pigpio:2.3.0")
    implementation("com.pi4j:pi4j-plugin-linuxfs:2.3.0")
}

//apply(plugin = "org.ros2.tools.gradle")

tasks.test {
    useJUnitPlatform()
}