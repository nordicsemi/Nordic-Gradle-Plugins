package no.nordicsemi.android

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

object AppConst {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    val KOTLIN_VERSION = KotlinVersion.KOTLIN_2_4
    val JAVA_SOURCE_VERSION = JavaVersion.VERSION_17
    val JAVA_TARGET_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}
