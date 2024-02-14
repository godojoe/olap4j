/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
}

group = "org.olap4j"
version = "1.3.0-SNAPSHOT"
description = "olap4j"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

sourceSets {
    main {
        java {
            srcDirs("$projectDir/src/generated", "$projectDir/src")
        }
    }
}


repositories {
    mavenLocal()
    /*maven {
        url = uri("http://repo.pentaho.org/artifactory/repo")

        // isAllowInsecureProtocol = true
    }*/
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}
tasks {
    register("parser", JavaExec::class) {
        group = "build"
        description = "Javacc compilation of MDX grammar"
        mainClass.set("javacc")
        doFirst {
            val javaCCJar = javaccparsergen.resolvedConfiguration.resolvedArtifacts.single { it.extension == "jar" }.file.absolutePath
            classpath = fileTree(javaCCJar)
            args = listOf("-OUTPUT_DIRECTORY=${projectDir}/src/generated/org/olap4j/mdx/parser/impl/", "${projectDir}/src/org/olap4j/mdx/parser/impl/MdxParser.jj")
        }
        dependsOn(javaccparsergen)
    }
}

val javaccparsergen = configurations.create("javaccparsergen")

dependencies {
    // api(libs.net.java.openjdk.ctsym.java7)
    api(libs.xerces.xercesimpl)
    api(libs.commons.dbcp.commons.dbcp)
    api(libs.com.h2database.h2)
    api(libs.mysql.mysql.connector.java)
    api("com.google.protobuf:protobuf-java") {
        version {
            strictly("3.11.0")
        }
    }
    api(libs.commons.pool.commons.pool)
    api(libs.commons.collections.commons.collections)
    api(libs.commons.vfs.commons.vfs)
    api(libs.commons.logging.commons.logging)
    api(libs.commons.math.commons.math)
    api(libs.javax.servlet.servlet.api)
    //api(libs.pentaho.mondrian.data.foodmart)
    javaccparsergen(libs.net.java.dev.javacc.javacc)
    testImplementation(libs.junit.junit)
    testImplementation(libs.org.apache.commons.commons.dbcp2)
    //testImplementation(libs.pentaho.mondrian)
    testImplementation(libs.log4j.log4j)
    //compileOnly(libs.com.sun.rt.jdk1.v5)
    //compileOnly(libs.net.java.openjdk.rt.java6)
}

java {
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.getByName("compileJava") {
    dependsOn(tasks.getByName("parser"))
}

tasks.getByName("clean") {
    delete("$projectDir/src/generated")
}