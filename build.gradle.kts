plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.pehrs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // FIXME: Need to use same version as Intellij !!!
    implementation("org.apache.velocity:velocity:1.7")

    // StringTemplate does not support calling methods on variables :-(
    // implementation("org.antlr:ST4:4.3.4")


    // Cannot use later versions of Velocity :-(
//    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")  {
//        exclude(group = "org.slf4j")
//    }
//    implementation("org.apache.velocity.tools:velocity-tools-view:3.1")  {
//        exclude(group = "org.slf4j")
//    }

    // Junit 5 is NOT supported by intellij SDK :-(
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.1.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
