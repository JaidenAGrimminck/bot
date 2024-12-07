plugins {
    id("java")
    id("application")
}

group = "me.autobot"
version = "0.1"

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

tasks.javadoc {
    destinationDir = file("docs/javadoc")

    source = sourceSets.main.get().allJava

    options.encoding = "UTF-8"
    options.windowTitle = "Bot (Control) Docs"
    title = "Bot (Control) Docs"

    //ignore any empty docs
    //options.jFlags("-Xdoclint:none")
}

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

    //pi4j plugins for pi gpio and linux fs
    implementation("com.pi4j:pi4j-ktx:2.4.0") // Kotlin DSL
    implementation("com.pi4j:pi4j-core:2.3.0")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:2.3.0")
    implementation("com.pi4j:pi4j-plugin-pigpio:2.3.0")
    implementation("com.pi4j:pi4j-plugin-linuxfs:2.3.0")

    //slf4j for logging
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.slf4j:slf4j-api:2.0.16")

    //nanohttpd for webserver
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("org.nanohttpd:nanohttpd-websocket:2.3.1")

    //just implement all files in lib folder implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation(files("libs/jSerialComm-2.6.2.jar"))

    // reflection for scanning classes
    implementation("org.reflections:reflections:0.10.2")

    // json for parsing json
    implementation("com.google.code.gson:gson:2.8.9")
}

//apply(plugin = "org.ros2.tools.gradle")

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "me.autobot.code.Main"
}
