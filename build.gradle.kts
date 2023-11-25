import deploy.default
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    application
    id("telegram-bot-convention")
    id("deploy-convention")
}

application {
    mainClass = "app.meetacy.telegram.bot.MainKt"
}

dependencies {
    implementation(libs.ktgbotapi)
    implementation(libs.meetacy.sdk.api.ktor)
}

val propertiesFile: File = rootProject.file("deploy.properties")

deploy {
    val isRunner = System.getenv("IS_RUNNER")?.toBoolean() == true
    val properties = if (propertiesFile.exists()) loadProperties(propertiesFile.absolutePath) else null

    if (!isRunner && properties == null) return@deploy

    default {
        host = properties?.getProperty("host") ?: System.getenv("SSH_HOST")
        user = properties?.getProperty("user") ?: System.getenv("SSH_USER")
        password = properties?.getProperty("password") ?: System.getenv("SSH_PASSWORD")
        knownHostsFile = properties?.getProperty("knownHosts") ?: System.getenv("SSH_KNOWN_HOST_FILE")
        archiveName = "app.jar"
        mainClass = "app.meetacy.telegram.bot.MainKt"
    }

    target("production") {
        destination = properties?.getProperty("prod.destination") ?: System.getenv("DEPLOY_DESTINATION")
        serviceName = properties?.getProperty("prod.serviceName") ?: System.getenv("DEPLOY_SERVICE_NAME")
    }
}
