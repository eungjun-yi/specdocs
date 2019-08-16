import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.3.41"

buildscript {
    val kotlinVersion = "1.3.41"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    java
    maven
    jacoco
    idea

    id("com.adarshr.test-logger") version "1.6.0"
}

apply {
    plugin("kotlin")
}

configure<JavaPluginConvention> {
    group = "im.toss"
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    implementation("org.junit.jupiter:junit-jupiter-params:5.5.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    compile("org.junit.platform:junit-platform-launcher:1.5.0")

    testImplementation("org.junit.vintage:junit-vintage-engine:5.5.1")
    testCompile("org.junit.platform:junit-platform-commons:1.5.0")
    testCompile("org.junit.platform:junit-platform-engine:1.5.0")
    testCompile("org.mockito:mockito-junit-jupiter:3.0.0") {
        exclude(group="org.junit.jupiter")
    }

    compile("org.javassist:javassist:3.25.0-GA")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.assertj:assertj-core:3.12.2")

    testImplementation("io.mockk:mockk:1.8.13")
    testImplementation("com.github.toss:assert-extensions:0.1.1")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks

compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
}

jacoco {
    toolVersion = "0.8.4"
}

tasks {
    test {
        useJUnitPlatform {
        }
    }

    jacocoTestReport {
        executionData.setFrom(
            fileTree("build/jacoco") {
                include("**/*.exec")
            }
        )

        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(setOf(jacocoTestReport))
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    minimum = "1.000000000".toBigDecimal()
                }

                limit {
                    counter = "BRANCH"
                    minimum = "1.000000000".toBigDecimal()
                }
            }
        }
    }
}

testlogger {
    theme = ThemeType.STANDARD_PARALLEL
    showExceptions = true
    slowThreshold = 2000
    showSummary = true
    showPassed = true
    showSkipped = true
    showFailed = true
    showStandardStreams = false
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = false
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
