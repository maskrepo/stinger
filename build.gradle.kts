val quarkusVersion: String = "1.8.0.Final"
val MaskModelVersion = "1.0.3-SNAPSHOT"
val MaskUtilVersion = "1.0.4-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id ("io.quarkus") version "1.8.0.Final"
    id ("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"
    id ("org.sonarqube") version "2.7"
    id ("jacoco")
    `maven-publish`
}

group = "fr.convergence.proddoc"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// je mets ces 2 variables ici car je n'arrive pas à les mettre ailleurs
// (dans settings.gradle.kts par exemple)
val myMavenRepoUser = "myMavenRepo"
val myMavenRepoPassword ="mask"

repositories {
    mavenLocal()
    maven {
        url = uri("https://mymavenrepo.com/repo/OYRB63ZK3HSrWJfc2RIB/")
        credentials {
            username = myMavenRepoUser
            password = myMavenRepoPassword
        }
    }
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("https://mymavenrepo.com/repo/ah37AFHxnt3Fln1mwTvi/")
            credentials {
                username = myMavenRepoUser
                password = myMavenRepoPassword
            }
        }
        mavenLocal()
    }

    publications {
        create<MavenPublication>("stinger") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation("io.quarkus:quarkus-resteasy:$quarkusVersion")
    implementation("io.quarkus:quarkus-rest-client:$quarkusVersion")
    implementation("io.quarkus:quarkus-kafka-client:$quarkusVersion")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka:$quarkusVersion")
    implementation("io.quarkus:quarkus-vertx-web:$quarkusVersion")
    implementation("io.vertx:vertx-web-client:3.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("org.jboss.slf4j:slf4j-jboss-logging:1.2.1.Final")

    implementation("fr.convergence.proddoc.lib:mask-model:$MaskModelVersion")
    implementation("fr.convergence.proddoc.lib:mask-util:$MaskUtilVersion")

    testImplementation("io.quarkus:quarkus-junit5")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.ws.rs.Path")
}