import java.util.zip.ZipFile

plugins {
    base
}

group = "org.tavall"
extra["versionTagPrefix"] = "TavallContractors"
apply(from = "gradle/git-version.gradle.kts")
version = extra["gitVersion"] as String

val springBootBom = "org.springframework.boot:spring-boot-dependencies:4.1.0"
val testcontainersVersion = "2.0.5"

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
        val token = providers.environmentVariable("GITHUB_TOKEN")
        if (token.isPresent) {
            maven {
                name = "TavallPackages"
                url = uri("https://maven.pkg.github.com/TavallStudios/tavall-cache")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = token.get()
                }
            }
        }
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val verifyJarContents = tasks.register("verifyJarContents") {
        dependsOn(tasks.named("jar"))
        val archive = tasks.named<Jar>("jar").flatMap { it.archiveFile }
        inputs.file(archive)
        doLast {
            val forbidden = listOf(
                "com/fasterxml/",
                "com/mongodb/",
                "jakarta/",
                "org/hibernate/",
                "org/postgresql/",
                "org/springframework/",
                "org/thymeleaf/",
            )
            ZipFile(archive.get().asFile).use { jar ->
                val embedded = jar.entries().asSequence().map { it.name }
                    .firstOrNull { entry -> forbidden.any(entry::startsWith) }
                check(embedded == null) { "Third-party class embedded in first-party JAR: $embedded" }
            }
        }
    }

    tasks.named("check") {
        dependsOn(verifyJarContents)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = project.name
            }
        }
        repositories {
            val token = providers.environmentVariable("GITHUB_TOKEN")
            if (token.isPresent) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/TavallStudios/TavallContractors")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = token.get()
                    }
                }
            }
        }
    }
}

project(":internal-contractor-api") {
    dependencies {
        "api"(platform(springBootBom))
        "api"("org.springframework:spring-web")
    }
}

project(":spring-webview") {
    apply(plugin = "application")

    extensions.configure<JavaApplication> {
        mainClass = "org.tavall.contractors.SpringWebviewApplication"
    }

    val sourceSets = extensions.getByType<SourceSetContainer>()
    val integrationTestSourceSet = sourceSets.create("integrationTest")
    configurations[integrationTestSourceSet.implementationConfigurationName]
        .extendsFrom(configurations["implementation"], configurations["testImplementation"])
    configurations[integrationTestSourceSet.runtimeOnlyConfigurationName]
        .extendsFrom(configurations["runtimeOnly"], configurations["testRuntimeOnly"])
    integrationTestSourceSet.compileClasspath += sourceSets.named("main").get().output
    integrationTestSourceSet.runtimeClasspath += sourceSets.named("main").get().output

    dependencies {
        "implementation"(platform(springBootBom))
        "testImplementation"(platform(springBootBom))
        "integrationTestImplementation"(platform(springBootBom))
        "implementation"(project(":internal-contractor-api"))
        "implementation"("org.tavall:abstract-cache-system:1.0.0")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("org.springframework.boot:spring-boot-starter-thymeleaf")
        "implementation"("org.springframework.boot:spring-boot-starter-security")
        "implementation"("org.springframework.boot:spring-boot-starter-oauth2-client")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-data-mongodb")
        "implementation"("org.springframework.boot:spring-boot-jackson2")
        "runtimeOnly"("org.postgresql:postgresql")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "integrationTestImplementation"(platform("org.junit:junit-bom:6.0.3"))
        "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
        "integrationTestImplementation"("org.springframework.boot:spring-boot-webmvc-test")
        "integrationTestImplementation"("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion")
        "integrationTestImplementation"("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
        "integrationTestImplementation"("org.testcontainers:testcontainers-mongodb:$testcontainersVersion")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "integrationTestRuntimeOnly"("org.apiguardian:apiguardian-api:1.1.2")
    }

    tasks.named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = "org.tavall.contractors.SpringWebviewApplication"
        }
    }

    val npmInstall = tasks.register<Exec>("npmInstall") {
        workingDir(projectDir)
        commandLine("npm", "ci")
        inputs.files("package.json", "package-lock.json")
        outputs.dir("node_modules")
    }

    val npmBuild = tasks.register<Exec>("npmBuild") {
        dependsOn(npmInstall)
        workingDir(projectDir)
        commandLine("npm", "run", "build")
        inputs.dir("src/main/resources/static/ts")
        inputs.dir("scripts")
        inputs.file("tsconfig.json")
        outputs.dir("src/main/resources/static/js")
    }

    tasks.named<ProcessResources>("processResources") {
        dependsOn(npmBuild)
    }

    tasks.named<Jar>("sourcesJar") {
        dependsOn(npmBuild)
    }

    tasks.register<Test>("integrationTest") {
        description = "Runs the PostgreSQL and MongoDB integration suite."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        shouldRunAfter(tasks.named("test"))
    }
}

val stageDistribution = tasks.register<Sync>("stageDistribution") {
    dependsOn(":spring-webview:jar")
    into(layout.projectDirectory.dir("distribution"))
    from(project(":spring-webview").tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
        rename { "application.jar" }
    }
    from(project(":spring-webview").configurations.named("runtimeClasspath")) {
        into("libs")
    }
}

tasks.named("assemble") {
    dependsOn(stageDistribution)
}
