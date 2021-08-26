plugins {
    java
    id("io.izzel.taboolib") version "1.26"
    id("org.cadixdev.licenser") version "0.6.1"
    kotlin("jvm") version "1.5.10"
}

group = "site.liangbai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

taboolib {
    install("common")
    install("module-chat")
    install("module-nms")
    install("platform-bukkit")
    classifier = null
    version = "6.0.0-pre62"
}

dependencies {
    compileOnly("ink.ptms.core:v11604:11604:all")
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    implementation(kotlin("stdlib"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

license {
    include("site/liangbai/**/*.java")
    include("site/liangbai/**/*.kt")

    header(rootProject.file("HEADER.txt"))

    ext {
        this["organization"] = "Liangbai"
        this["year"] = 2021
    }
}