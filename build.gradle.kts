plugins {
    kotlin("multiplatform") version "2.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.2")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }
}
