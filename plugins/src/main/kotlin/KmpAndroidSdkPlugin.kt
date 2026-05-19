import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.dsl.LibraryExtension
import no.nordicsemi.android.AppConst
import no.nordicsemi.hasAndroidAppPlugin
import no.nordicsemi.hasAndroidKmpPlugin
import no.nordicsemi.hasAndroidLibPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpAndroidSdkPlugin : Plugin<Project> {

    private fun configureAndroidKmpPlugin(target: Project) {
        if (!target.hasAndroidKmpPlugin) return
        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinMultiplatformAndroidLibraryTarget> {
                    compileSdk = AppConst.COMPILE_SDK
                    minSdk = AppConst.MIN_SDK
                    // todo current version of Jetbrains Compose Resources doesn't handle new android resourceless plugin
                    androidResources.enable = true
                }
            }
        }
    }

    private fun configureAndroidLibraryExtension(target: Project) {
        if (!target.hasAndroidLibPlugin) return
        target.extensions.configure<LibraryExtension> {
            compileSdk = AppConst.COMPILE_SDK

            defaultConfig {
                minSdk = AppConst.COMPILE_SDK
            }
        }
    }

    private fun configureAndroidApplicationExtension(target: Project) {
        if (!target.hasAndroidAppPlugin) return
        target.extensions.configure<ApplicationExtension> {
            compileSdk = AppConst.COMPILE_SDK

            defaultConfig {
                minSdk = AppConst.MIN_SDK
                targetSdk = AppConst.TARGET_SDK
            }
        }
    }

    override fun apply(target: Project) {
        configureAndroidLibraryExtension(target)
        configureAndroidApplicationExtension(target)
        configureAndroidKmpPlugin(target)
    }
}