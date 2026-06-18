plugins {
    application
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":modules:compiler:core"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.1")
}

application {
    mainClass.set("com.orca.lsp.OrcaLspLauncher")
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("fat")
    manifest {
        attributes["Main-Class"] = "com.orca.lsp.OrcaLspLauncher"
    }
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Signed JARs (Guava, LSP4J) embed signature files that become invalid once merged
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC")
}
