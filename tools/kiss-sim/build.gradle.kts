plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.kj6ywd.packet.sim.MainKt")
}

dependencies {
    implementation(project(":packet-core"))
    implementation(project(":kiss"))
}
