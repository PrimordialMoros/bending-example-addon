plugins {
    java
}

group = "me.moros"
version = "1.9.0"

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral() // for bending-api releases
    maven("https://central.sonatype.com/repository/maven-snapshots/") // for bending-api snapshots
}

dependencies {
    compileOnly("me.moros:bending-api:3.16.0")
}
