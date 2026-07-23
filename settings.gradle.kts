plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TavallContractors"

include(
    "internal-contractor-api",
    "spring-webview",
)
