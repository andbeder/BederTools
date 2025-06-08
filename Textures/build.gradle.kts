/*
 * build.gradle.kts – Textures application configuration
 */

plugins {
    `java-library`
    `maven-publish`
    application
    eclipse
}

group = "com.beder"
version = "0.0.1-SNAPSHOT"
description = "Textures"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

sourceSets {
    main {
        // Keep your existing src/ folder rather than the standard src/main/java
        java.srcDirs("src")
    }
}

dependencies {
    implementation("com.miglayout:miglayout-swing:5.3")
    implementation("org.locationtech.jts:jts-core:1.20.0")
}

application {
    mainClass.set("com.beder.texture.TextureGenius")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

// --------------------------------------------
// Eclipse plugin configuration
// --------------------------------------------
eclipse {
  classpath {
    plusConfigurations = mutableSetOf(
      configurations.getByName("runtimeClasspath")
    )

    // or add more in one go:
    // plusConfigurations = mutableSetOf(
    //   configurations.getByName("runtimeClasspath"),
    //   configurations.getByName("otherConfig")
    // )

    containers("org.eclipse.buildship.core.gradleclasspathcontainer")
  }
}

