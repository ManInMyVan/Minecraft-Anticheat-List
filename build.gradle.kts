plugins {
    kotlin("multiplatform") version "1.9.24"
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.8.1")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }
}
