plugins {
    java
}

group = "me.moros"
version = "1.3.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // for bending-api snapshots
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("me.moros", "bending-api", "3.0.0-SNAPSHOT")
    compileOnly("io.papermc.paper", "paper-api", "1.19.3-R0.1-SNAPSHOT")
}

tasks {
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand("pluginVersion" to project.version)
        }
    }
}
