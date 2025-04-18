plugins {
    id("java")
    `maven-publish`

    // Nothing special about this, just keep it up to date
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false

    // In general, keep this version in sync with upstream. Sometimes a newer version than upstream might work, but an older version is extremely likely to break.
    //id("io.papermc.paperweight.patcher") version "1.7.3"
}

repositories {
    //mavenLocal()
    mavenCentral()
    maven {
        name = "Verdox Reposilite"
        url = uri("https://repo.verdox.de/snapshots")
    }
}

dependencies {
    //compileOnly("de.verdox.mccreativelab:mcc-util:" + providers.gradleProperty("version").get())
    compileOnly("de.verdox.mccreativelab.mcc-wrapper:api:" + providers.gradleProperty("version").get())
    compileOnly("de.verdox:vserializer:+")
    compileOnly("org.jetbrains:annotations:26.0.1")
    compileOnly("io.vertx:vertx-core:4.5.10")
    compileOnly("com.hierynomus:sshj:0.38.0")
    compileOnly("org.tukaani:xz:1.9")
    compileOnly("commons-io:commons-io:2.17.0")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("org.apache.commons:commons-lang3:3.17.0")
    compileOnly("ws.schild:jave-all-deps:3.5.0")
    compileOnly("com.google.guava:guava:33.3.1-jre")
    compileOnly("org.apache.commons:commons-compress:1.27.1")


    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                groupId = "de.verdox.mccreativelab"
                artifactId = "mcc-pack-generator"
                version = providers.gradleProperty("version").get()
                from(components["java"])
                licenses {
                    license {
                        name = "GNU GENERAL PUBLIC LICENSE Version 3"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "verdox"
                        name = "Lukas Jonsson"
                        email = "mail.ysp@web.de"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "verdox"
            url = uri("https://repo.verdox.de/snapshots")
            credentials {
                username = (findProperty("reposilite.verdox.user") ?: System.getenv("REPO_USER")).toString()
                password = (findProperty("reposilite.verdox.key") ?: System.getenv("REPO_PASSWORD")).toString()
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}