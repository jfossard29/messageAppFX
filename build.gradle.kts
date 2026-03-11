plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.media")
}

application {
    mainClass.set("com.Main")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "src/main/java")
            exclude("**/*.java")
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Compile le projet en jar"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.Main"
    }
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.register("cleanMessages") {
    group = "cleanup"
    description = "Supprime tous les fichiers .msg du dossier echanges"
    doLast {
        val echangesDir = file("src/main/resources/echanges")
        if (echangesDir.exists()) {
            echangesDir.walk()
                .filter { it.isFile && it.extension == "msg" }
                .forEach { 
                    println("Deleting: ${it.name}")
                    it.delete() 
                }
        } else {
            println("Directory not found: ${echangesDir.absolutePath}")
        }
    }
}
