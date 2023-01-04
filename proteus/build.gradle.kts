import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI;
plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "dev.julianhartl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = URI("https://maven.pkg.github.com/julian-hartl/proteus")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("lang.proteus:proteus-compiler-kotlin:1.0-SNAPSHOT") {
        isChanging = true
    }
}
configurations.all {
    // Check for updates every build
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
