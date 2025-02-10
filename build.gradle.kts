/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2025  Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    id("signing")
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven(uri("https://repo.papermc.io/repository/maven-public/"))
    maven(uri("https://maven.plugily.xyz/releases"))
    maven(uri("https://maven.plugily.xyz/snapshots"))
    maven(uri("https://repo.citizensnpcs.co/"))
    maven(uri("https://repo.maven.apache.org/maven2/"))
    maven(uri("https://repo.dmulloy2.net/repository/public/"))
    maven(uri("https://maven.citizensnpcs.co/repo"))
}



dependencies {
    implementation(files("lib/box-classic.jar"))
    implementation(files("lib/box-db.jar"))
    implementation(files("lib/box-inv.jar"))
    implementation(files("lib/box-utils.jar"))
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.17")
    implementation("org.openjdk.nashorn:nashorn-core:15.1")
    implementation("fr.skytasul:glowingentities:1.4.2")
    implementation("fr.skytasul:guardianbeam:2.4.0")
    compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")

    compileOnly("com.mojang:authlib:3.11.50")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    compileOnly(files("lib/spigot/1.8.8-R0.1.jar"))

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

group = "plugily.projects"
version = "5.0.1-SNAPSHOT6"
description = "VillageDefense"
java {
    withJavadocJar()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }
    }

    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }
        archiveClassifier.set("")
        relocate("plugily.projects.minigamesbox", "plugily.projects.villagedefense.minigamesbox")
        relocate("com.zaxxer.hikari", "plugily.projects.villagedefense.database.hikari")
        minimize()
    }

    processResources {
        filesMatching("**/plugin.yml") {
            expand(project.properties)
        }
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    compileJava {
        options.encoding = "UTF-8"
        targetCompatibility = JavaVersion.VERSION_17.toString()
        sourceCompatibility = JavaVersion.VERSION_17.toString()
    }

}

publishing {
    repositories {
        maven {
            name = "Releases"
            url = uri("https://maven.plugily.xyz/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "Snapshots"
            url = uri("https://maven.plugily.xyz/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}