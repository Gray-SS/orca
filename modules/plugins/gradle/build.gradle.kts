plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

gradlePlugin {
    plugins {
        create("gradlePlugin") {
            id = "com.orca"
            implementationClass = "com.orca.gradle.OrcaPlugin"
        }
    }
}

dependencies {
    implementation(project(":modules:compiler:core"))
}

// Bundle stdlib .orca sources inside the plugin JAR so any consumer gets them
// without needing to publish the stdlib to Maven.
tasks.named<ProcessResources>("processResources") {
    val stdlibSrcDir = rootProject.project(":modules:stdlib").projectDir.resolve("src/main/orca")
    from(stdlibSrcDir) {
        into("com/orca/gradle/stdlib")
    }
    // Generate a manifest listing every bundled stdlib source (one relative path per line).
    // ExtractStdlibTask reads this manifest at build time instead of scanning the JAR directly.
    doLast {
        val stdlibDir = destinationDir.resolve("com/orca/gradle/stdlib")
        val manifestFile = destinationDir.resolve("com/orca/gradle/stdlib-manifest.txt")
        if (stdlibDir.exists()) {
            manifestFile.writeText(
                stdlibDir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.relativeTo(stdlibDir).path.replace('\\', '/') }
                    .sorted()
                    .joinToString("\n")
            )
        }
    }
}