plugins {
    kotlin("multiplatform") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.0")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }
}
