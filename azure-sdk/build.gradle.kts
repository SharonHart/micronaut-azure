plugins {
    id("io.micronaut.build.internal.azure-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mn.micronaut.inject.java)
    implementation(mn.micronaut.runtime)
    implementation(platform(projects.azureBom))
    api(libs.azure.identity)
    compileOnly(libs.azure.storage.common)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(libs.azure.storage.blob)
    testImplementation(libs.system.lambda)
    testRuntimeOnly(libs.azure.storage.common)
}
