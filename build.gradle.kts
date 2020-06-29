import java.net.URI

plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "com.exolius.simplebackup"
version = "1.8"
application.mainClassName = "com.exolius.simplebackup.SimpleBackup"

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
    implementation("org.spigotmc:spigot-api:1.11.2-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

tasks {
    shadowJar {
        manifest {
            attributes.apply {
                this["Main-Class"] = application.mainClassName
            }
        }
    }
    wrapper {
        gradleVersion = "6.5"
        distributionType = Wrapper.DistributionType.ALL
    }
}
