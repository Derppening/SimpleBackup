import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = URI("https://oss.sonatype.org/content/repositories/central") }
}

dependencies {
    kapt("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")

    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    implementation("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.15.1")
}

kotlin {
    jvmToolchain(17)
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
        gradleVersion = "8.6"
    }
}
