plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
}
dependencies {
    testImplementation(projects.micronautAzureFunctionHttp)
    testImplementation(libs.managed.azure.functions.java.library)
}
