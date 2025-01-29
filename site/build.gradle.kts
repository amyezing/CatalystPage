import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    // alias(libs.plugins.kobwebx.markdown)
}

group = "catalystpage.com"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Catalyst Beverage Manufacturing")

            head.add {
                script {
                    src = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                }
                link {
                    href = "https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap"
                    rel = "stylesheet"
                }
                link {
                    rel = "stylesheet"
                    href = "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
                }
            }


        }


    }
}

kotlin {
    configAsKobwebApplication("com", includeServer = true)

    sourceSets {
//        commonMain.dependencies {
//          // Add shared dependencies between JS and JVM here
//        }
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(libs.silk.icons.fa)
            // implementation(libs.kobwebx.markdown)
            
        }
        jvmMain.dependencies {
            compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
        }
    }
}

