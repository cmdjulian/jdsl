plugins {
    kotlin("jvm") version "2.3.0"

    jacoco
    `java-library`
    `maven-publish`

    // check for dependency updates via task "dependencyUpdates --refresh-dependencies"
    id("com.github.ben-manes.versions") version "0.53.0"
    // linting
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

// Set project coordinates for publishing. Allow overriding via -Pgroup / -Pversion (used by JitPack)
group = findProperty("group")?.toString() ?: "de.cmdjulian"
version = findProperty("version")?.toString() ?: "2.0.0"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    implementation(platform("tools.jackson:jackson-bom:3.0.3"))
    implementation("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("io.kotest:kotest-runner-junit5:6.0.7")
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.7")
    testImplementation("io.kotest:kotest-assertions-json-jvm:6.0.7")
}

tasks {
    val sourcesJar by registering(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }

    jar {
        archiveBaseName.set("jdsl")
    }
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

        configuration
            .sortedBy { -it.length() }
            .forEach {
                out.append(it.name.padEnd(65))
                out.append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
            }
    } else {
        out.append("No dependencies found")
    }
    println(out)
}

publishing {
    publications {
        create<MavenPublication>("jdsl") {
            groupId = project.group.toString()
            artifactId = "jdsl"
            version = project.version.toString()

            from(components["java"])
            artifact(tasks["sourcesJar"])

            pom {
                packaging = "jar"
                name.set("jdsl")
                description.set("kotlin dsl for jackson object mapper to describe json structures typesafe as code")
                url.set("https://github.com/cmdjulian/jdsl")
                scm {
                    url.set("https://github.com/cmdjulian/jdsl")
                }
                issueManagement {
                    url.set("https://github.com/cmdjulian/jdsl/issues")
                }
                developers {
                    developer {
                        id.set("cmdjulian")
                        name.set("Julian Goede")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cmdjulian/jdsl")
            credentials {
                // Expect env vars to be provided by CI or local shell
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}
