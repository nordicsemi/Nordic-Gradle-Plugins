package no.nordicsemi.android

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

object AppConst {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37

    /**
     * The lowest `compileSdk` a consumer of a published Nordic library may use.
     *
     * Since AGP 9.0 a library's AAR metadata defaults `minCompileSdk` to whatever the
     * library itself compiled against, so without setting this, every Nordic release
     * would require every consumer to move to the newest platform at the same time.
     * The AGP 9.0 release notes recommend setting `AarMetadata.minCompileSdk`
     * explicitly "if you want to give consumers of a library you publish more time to
     * switch".
     *
     * This value is not derived from [COMPILE_SDK] and should not track it. What
     * matters is the oldest platform a consumer can still compile a library's public
     * API and resources against — for a library that ships no resources, that is no
     * higher than its `minSdk`. Raise it only when a library genuinely uses a resource
     * or manifest attribute from a newer platform, and prefer raising it for that
     * library alone rather than for every published module.
     */
    const val MIN_COMPILE_SDK = 36
    val KOTLIN_VERSION = KotlinVersion.KOTLIN_2_4
    val JAVA_SOURCE_VERSION = JavaVersion.VERSION_17
    val JAVA_TARGET_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}
