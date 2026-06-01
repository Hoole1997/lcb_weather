import java.util.Properties
import org.gradle.authentication.http.BasicAuthentication

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

val buildConfigFile = file("build.config.properties")
val buildConfig = Properties()
if (buildConfigFile.exists()) {
    buildConfig.load(buildConfigFile.inputStream())
}

fun Properties.stringValue(name: String): String? =
    getProperty(name)?.takeIf { it.isNotBlank() }

fun envValue(name: String): String? =
    System.getenv(name)?.takeIf { it.isNotBlank() }

val githubPackagesUser = buildConfig.stringValue("github.user")
    ?: envValue("GH_PACKAGES_USER")
    ?: envValue("GITHUB_ACTOR")
val githubPackagesToken = buildConfig.stringValue("github.token")
    ?: envValue("GH_PACKAGES_TOKEN")
    ?: envValue("GITHUB_TOKEN")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                excludeGroup("com.github.toukaremax")
            }
        }
        maven("https://artifact.bytedance.com/repository/pangle/")
        maven("https://repo.itextsupport.com/android")
        maven("https://repo.dgtverse.cn/repository/maven-public/")
        maven("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven("https://android-sdk.is.com/")
        maven("https://jfrog.anythinktech.com/artifactory/overseas_sdk")
        maven("https://artifacts.applovin.com/android") {
            content {
                excludeGroup("com.github.toukaremax")
            }
        }
        maven("https://repo.dgtverse.cn/repository/maven-public")
        maven {
            url = uri("https://maven.pkg.github.com/toukaRemax/remax_sdk")
            credentials {
                username = githubPackagesUser
                password = githubPackagesToken
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
            content {
                includeGroup("com.github.toukaremax")
            }
        }
    }
}

rootProject.name = "LCB_Weather"
include(":app")
//include(":bill")
//include(":core")
include(":metrics")
