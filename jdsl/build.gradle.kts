plugins {
    kotlin("jvm") version "1.7.20"

    `java-library`
    // Gradle task "dependencyCheckAnalyze" to check for security CVEs in dependencies
    id("org.owasp.dependencycheck") version "7.3.0"
    // Check for dependency updates via task "dependencyUpdates --refresh-dependencies"
    id("com.github.ben-manes.versions") version "0.43.0"
    // linting
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    implementation(platform("com.fasterxml.jackson:jackson-bom:2.13.4"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    val kotest = "5.5.2"
    testImplementation("io.kotest:kotest-runner-junit5:$kotest")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
    testImplementation("io.kotest:kotest-assertions-json-jvm:$kotest")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register("depsize") {
    description = "Prints dependencies for \"default\" configuration"
    doLast {
        listConfigurationDependencies(configurations["default"])
    }
}

tasks.register("depsize-all-configurations") {
    description = "Prints dependencies for all available configurations"
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { listConfigurationDependencies(it) }
    }
}

fun listConfigurationDependencies(configuration: Configuration) {
    val formatStr = "%,10.2f"

    val size = configuration.sumOf { it.length() / (1024.0 * 1024.0) }

    val out = StringBuffer()
    out.append("\nConfiguration name: \"${configuration.name}\"\n")
    if (size > 0) {
        out.append("Total dependencies size:".padEnd(65))
        out.append("${String.format(formatStr, size)} Mb\n\n")

        configuration.sortedBy { -it.length() }
            .forEach {
                out.append(it.name.padEnd(65))
                out.append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
            }
    } else {
        out.append("No dependencies found")
    }
    println(out)
}
