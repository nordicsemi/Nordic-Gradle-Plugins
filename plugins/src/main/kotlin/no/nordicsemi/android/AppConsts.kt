package no.nordicsemi.android

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

object AppConst {
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    val KOTLIN_VERSION = KotlinVersion.KOTLIN_2_3
    val JAVA_SOURCE_VERSION = JavaVersion.VERSION_17
    val JAVA_TARGET_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}
