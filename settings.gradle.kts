rootProject.name = "telegram-bot"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

class GitHubUnauthorizedException(env: String)
    : Exception("We could not find your credentials for GitHub. Check if the $env environment variable set")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven {
            url = uri("https://maven.pkg.github.com/meetacy/maven")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                    ?: System.getenv("USERNAME")
                            ?: throw GitHubUnauthorizedException("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN") ?: throw GitHubUnauthorizedException("GITHUB_TOKEN")
            }
        }
    }
}

includeBuild("build-logic")
