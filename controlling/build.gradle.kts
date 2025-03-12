plugins {
    id("java")
    id("application")
    id("org.jetbrains.kotlin.jvm") version "1.7.20" // Use the appropriate version for your project
}

group = "me.autobot"
version = "0.2"

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

//    dependencies {
//        classpath("gradle.plugin.org.ros2.tools.gradle:ament:0.7.0")
//    }
}

tasks.javadoc {
    setDestinationDir(file("docs/javadoc"))

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

application {
    mainClass.set("me.autobot.code.Main")
}

// jar
tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.autobot.code.Main"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(
        *configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
            .toTypedArray()
    )
}

tasks.register("simulate") {
    // same as run, but change main class to me.autobot.simulation.Simulate
    //TODO: change main class to me.autobot.simulation.Simulate
    doFirst {
        println("Simulating...")
    }

    // etc.

}

//new task, uploading
tasks.register("upload") {
    doFirst {
        val reset = "\u001b[0m"
        val yellow = "\u001b[33m"
        val blue = "\u001b[34m"

        println("${yellow}First, building the project and creating a ${blue}.jar${yellow} file!$reset")
        println("...")
        println("...")
    }

    //build, jar
    dependsOn("build")
    dependsOn("jar")

    //then, we'll run the "upload" task
    doLast {
        val reset = "\u001b[0m"
        val green = "\u001b[32m"
        val red = "\u001b[31m"
        val yellow = "\u001b[33m"
        val blue = "\u001b[34m"
        val italicsBlue = "\u001b[3;34m"
        val italicsPurple = "\u001b[3;35m"
        val greenBold = "\u001b[1;32m"

        println("${green}Finished building!")
        //upload to server
        println("${yellow}Checking server arguments...")

        // get the first argument
        var server = project.findProperty("server") as String?

        //if server is null, then we'll use the default server
        if (server == null) {
            println("${red}No server provided, using default server")
            server = "localhost";
        } else {
            println("${green}Server: $server")
        }

        // move the jar file into ./latest-build
        val jarFile = file("build/libs/${project.name}-${project.version}.jar")
        val latestBuild = file("latest-build/${project.name}-${project.version}.jar")

        // copy the jar file to the latest-build folder
        jarFile.copyTo(latestBuild, true)

        println("${green}Successfully locally copied ${blue}.jar${green} file to the ${yellow}latest-build${green} folder.")

        val username = project.findProperty("username") as String?
        var destinationDir = "~/"

        if (username == null) {
            println("${red}Stopping here, details are undefined.")
            println("${italicsBlue}Please provide:\n - ${italicsPurple}`server`${italicsBlue} (IP address of server),\n - ${italicsPurple}`username`${italicsBlue} (username of user on server),\n - and ${italicsPurple}`destinationDir`${italicsBlue} (directory on server to upload to) as arguments.")
            println("${green}Successfully built at ${greenBold}./latest-build/${project.name}-${project.version}.jar${reset}${green}!$reset")
        } else {
            println("${yellow}Attempting to upload to server...${reset}")

            exec {
                commandLine("sh", "upload.sh", server, username, destinationDir)
            }
        }
    }
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
    implementation(kotlin("script-runtime"))

}

//apply(plugin = "org.ros2.tools.gradle")

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "me.autobot.code.Main"
}
