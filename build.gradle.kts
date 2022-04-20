import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    application
    kotlin("jvm") version "1.6.20"
    kotlin("kapt") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
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

    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    implementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.11.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    shadowJar {
        manifest {
            attributes.apply {
                this["Main-Class"] = application.mainClass.get()
            }
        }
    }
    wrapper {
        gradleVersion = "7.4.2"
        distributionType = Wrapper.DistributionType.BIN
    }
}
