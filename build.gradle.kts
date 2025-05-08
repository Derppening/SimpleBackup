import java.net.URI

plugins {
    application
    kotlin("jvm") version "2.1.20"
    kotlin("kapt") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.derppening.simplebackupkt"
version = "2.0"

application {
    mainClass.set("com.derppening.simplebackupkt.SimpleBackup")
}

/*
https://www.spigotmc.org/wiki/spigot-plugin-development/
https://www.spigotmc.org/wiki/plugin-snippets/

*/
repositories {
    mavenCentral()
    maven {
        url = URI("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
}

dependencies {
    kapt("org.spigotmc:plugin-annotations:1.3-SNAPSHOT")

    compileOnly("org.spigotmc:plugin-annotations:1.3-SNAPSHOT")
    implementation("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.19.0")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        manifest {
            attributes.apply {
                this["Main-Class"] = application.mainClass.get()
            }
        }
    }
    wrapper {
        gradleVersion = "8.14"
    }
}
