plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<Test>("test") {
    systemProperty("orca.root.path", rootProject.rootDir.absolutePath)
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.ow2.asm:asm:9.7.1")
    testImplementation("junit:junit:4.13.2")
}