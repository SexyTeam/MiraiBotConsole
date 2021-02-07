plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
    `java-library`
    application
}

group = "club.eridani"
version = "1.0.1"

repositories {
    mavenCentral()
    jcenter()
}


val shade by configurations.creating

configurations {
    implementation.get().extendsFrom(shade)
}

dependencies {
    shade(kotlin("stdlib"))
    val miraiVersion = "2.3.2" // 替换为你需要的版本号
    api("net.mamoe", "mirai-core", miraiVersion)


    shade("net.mamoe:mirai-core-jvm:$miraiVersion") {
        exclude("net.mamoe","mirai-core-api")
        exclude("net.mamoe","mirai-core-utils")
    }

    shade("net.mamoe:mirai-core-api-jvm:$miraiVersion") {
        exclude("net.mamoe", "mirai-core-utils")
    }
    shade("net.mamoe:mirai-core-utils-jvm:$miraiVersion")

    shade("no.tornado:tornadofx:1.7.20")
    shade("org.jfxtras:jmetro:8.6.5")
    shade("org.controlsfx:controlsfx:8.40.18")
    shade("com.charleskorn.kaml:kaml:0.26.0")
    shade("no.tornado:tornadofx-controlsfx:0.1.1")
    val kotlinVersion = "1.4.30"
    shade(kotlin("reflect"))
    shade(kotlin("script-runtime"))
    shade(kotlin("script-util"))
    shade(kotlin("compiler-embeddable"))
    shade(kotlin("scripting-compiler-embeddable"))
    shade(kotlin("script-util"))
    shade("net.java.dev.jna:jna:4.2.2")
}


tasks.create<Copy>("downloadLibs") {
    from(configurations.default)
    into("libs")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}


fun findProperty(s: String) = project.findProperty(s) as String?
bintray {
    user = findProperty("bintrayUser")
    key = findProperty("bintrayApiKey")
    publish = true
    setPublications("QBotConsole")
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "mirai"
        userOrg = "eridani"
        setLabels("kotlin")
        setLicenses("MIT")
    })
}

publishing {
    publications {
        create<MavenPublication>("QBotConsole") {
            artifactId = "qbotconsole"
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.languageVersion = "1.4"
    }
}

tasks.withType<Jar> {
    from(shade.map {if (it.isDirectory) it else zipTree(it)})
    exclude("**/*.RSA")
    exclude("**/*.SF")
    exclude("LICENSE.txt")
    exclude("**/module-info.class")
    manifest {
        attributes["Main-Class"] = "club.eridani.qbotconsole.BotConsoleKt"
    }
}
