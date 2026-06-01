plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.sort.dependencies)
    alias(libs.plugins.maven.publish)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}
dependencies {
    testImplementation(libs.junit)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "io.github.joyner-perez",
        artifactId = "SpainNotebook64",
        version = "1.0.0",
    )

    pom {
        name.set("GoogleSignInButtonLibrary")
        description.set(
            "Kotlin library to read barcodes on financial documents in Spain.",
        )
        inceptionYear.set("2026")
        url.set("https://github.com/joyner-perez/SpainNotebook64")
        licenses {
            license {
                name.set("MIT")
                url.set("https://mit-license.org")
                distribution.set("https://mit-license.org")
            }
        }
        developers {
            developer {
                id.set("Joyner")
                name.set("Joyner")
                url.set("https://github.com/joyner-perez")
            }
        }
        scm {
            url.set("https://github.com/joyner-perez/SpainNotebook64")
            connection.set("scm:git://github.com/joyner-perez/SpainNotebook64.git")
            developerConnection.set(
                "scm:git:ssh://git@github.com/joyner-perez/SpainNotebook64.git",
            )
        }
    }
}
