plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TavallContractors"

val tavallToolsVersion = "1.0.0"
val tavallToolRepositories = listOf(
    "tavall-di",
    "tavall-cache",
    "tavall-concurrency",
    "tavall-database",
    "tavall-eventbus",
    "tavall-logging",
    "tavall-reflection",
    "tavall-registry",
    "tavall-scheduler",
)

gradle.beforeProject { project ->
    val githubToken = providers.environmentVariable("GITHUB_TOKEN").orNull
    if (!githubToken.isNullOrBlank()) {
        tavallToolRepositories.forEach { repository ->
            project.repositories.maven("https://maven.pkg.github.com/TavallStudios/$repository") {
                name = "github${repository.replace("-", "")}"
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orElse("github").get()
                    password = githubToken
                }
            }
        }
    }

    project.pluginManager.withPlugin("java") {
        project.dependencies.add("implementation", "org.tavall:tavall-di:$tavallToolsVersion")

        if (project.path == ":spring-webview") {
            project.dependencies.add("implementation", "org.tavall:tavall-database-postgres:$tavallToolsVersion")
            project.dependencies.add("implementation", "org.tavall:tavall-database-mongo:$tavallToolsVersion")
            project.dependencies.add("implementation", "org.tavall:tavall-logging:$tavallToolsVersion")
            project.dependencies.add("implementation", "org.tavall:tavall-concurrency:$tavallToolsVersion")
        }
    }
}

include(
    "internal-contractor-api",
    "spring-webview",
)
