plugins {
    id("io.micronaut.build.internal.azure-example")
}

micronaut {
    version("4.0.0-M2")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    compileOnly(mn.micronaut.inject.groovy)
    implementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.jakarta.inject.api)
    implementation(libs.managed.azure.functions.java.library)
}
