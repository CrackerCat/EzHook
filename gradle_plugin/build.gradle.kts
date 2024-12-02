import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-gradle-plugin")
    `maven-publish`
    kotlin("jvm")
    kotlin("kapt")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    // compiler plugin
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation(kotlin("gradle-plugin", version = "2.0.21"))
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.xdmrwu", "ez-hook-gradle-plugin", "0.0.2")

    pom {
        description.set("An AOP framework for KotlinMultiplatform, supporting Kotlin/Native and Kotlin/JS")
        url.set("https://github.com/XDMrWu/EzHook/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("XDMrWu")
                name.set("wulinpeng")
                url.set("https://github.com/XDMrWu/")
            }
        }
        scm {
            url.set("https://github.com/XDMrWu/EzHook")
            connection.set("scm:git:git://github.com/XDMrWu/EzHook.git")
            developerConnection.set("scm:git:ssh://git@github.com/XDMrWu/EzHook.git")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

gradlePlugin {
    plugins {
        create("EzHookPlugin") {
            id = "io.github.xdmrwu.ez-hook-gradle-plugin"
            displayName = "EzHook Compiler Plugin"
            description = "EzHook Compiler Plugin"
            implementationClass = "com.wulinpeng.ezhook.EzHookGradlePlugin"
        }
    }
}
