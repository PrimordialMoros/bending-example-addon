plugins {
    java
}

group = "me.moros"
version = "1.8.0"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://central.sonatype.com/repository/maven-snapshots/") // for bending-api snapshots
}

dependencies {
    compileOnly("me.moros", "bending-api", "3.15.0")
}
