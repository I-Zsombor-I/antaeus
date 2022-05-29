plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    api(project(":pleo-antaeus-models"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")

    //testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.7")
}

tasks.test {
    useJUnit()
}