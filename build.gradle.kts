plugins {
    // NOTE: bump this to whatever AGP 9.0+ version your Android Studio actually
    // has installed — this sandbox has no network access to Google's Maven so
    // the exact release string couldn't be verified here.
    id("com.android.application") version "8.5.2" apply false
    // The Kotlin Gradle plugin (org.jetbrains.kotlin.android) is intentionally
    // NOT declared here: AGP 9.0+ has Kotlin support built in, and applying it
    // explicitly now errors with "no longer required".
    // Compose's compiler is a separate plugin since Kotlin 2.0 (it used to be
    // configured via composeOptions.kotlinCompilerExtensionVersion instead).
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    // Keep this in sync with whatever Kotlin version AGP 9 embeds — KSP
    // versions are suffixed to a specific Kotlin release (KOTLIN-KSP.PATCH).
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}
