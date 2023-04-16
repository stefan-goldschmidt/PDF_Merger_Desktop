plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "org.pdfmerger"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.apache.pdfbox:pdfbox:2.0.28")
    implementation("org.apache.pdfbox:preflight:2.0.28")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application() {
    mainClass.set("org.pdfmerger.Main")
}

javafx {
    version = "20"
    modules("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing")
}

tasks.test {
    useJUnitPlatform()
}

jlink {
    launcher {
        name = "PDFMerger Desktop"
    }
}

