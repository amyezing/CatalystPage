import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kotlin.serialization)
}

group = "catalystpage.com"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Catalyst Beverage Manufacturing")

            head.add {
                script {
                    src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
                }
                link {
                    href = "https://fonts.googleapis.com/css2?family=Cutive+Mono&display=swap"
                    rel = "stylesheet"
                }
                link {
                    rel = "stylesheet"
                    href = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
                }
                script {
                    src = "https://accounts.google.com/gsi/client"
                    async = true
                    defer = true
                }
                link {
                    href = "https://fonts.googleapis.com/css2?family=Roboto+Serif:ital,opsz,wght@0,8..144,100..900;1,8..144,100..900&display=swap"
                    rel = "stylesheet"
                }
                link {
                    rel = "stylesheet"
                    href = "style.css"
                }
                link {
                    href = "https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400..900;1,400..900&display=swap"
                    rel = "stylesheet"
                }
            }
        }
    }
}

kotlin {
    configAsKobwebApplication("catalystpage.com", includeServer = true)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
        }
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(libs.silk.icons.fa)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(npm("firebase", "11.10.0"))
            implementation(npm("html5-qrcode", "2.3.8"))
            implementation(npm("qrcode", "1.5.4"))
        }
        jvmMain.dependencies {
            compileOnly(libs.kobweb.api)
            implementation(libs.mariadb.jdbc)
            implementation(libs.ktor.core)
            implementation(libs.ktor.netty)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.call.logging)
            implementation(libs.ktor.auth)
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.hikari.cp)
            implementation(libs.date.time)
            implementation(libs.kotlinx.datetime)
            implementation(libs.server.cors)
            implementation(libs.google.cloud)
            implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
            implementation("com.sun.mail:jakarta.mail:2.0.1")
            implementation(libs.ktor.websocket)
            implementation("org.slf4j:slf4j-simple:2.0.9")
        }
    }
}

tasks.named<Jar>("jvmJar") {
    archiveBaseName.set("catalyst-backend")
    archiveFileName.set("catalyst-backend.jar") // ✅ Explicit output name

    manifest {
        attributes["Main-Class"] = "catalystpage.com.ApplicationKt"
    }

    // Include all runtime dependencies to create a fat JAR
    from(configurations.getByName("jvmRuntimeClasspath").map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}
