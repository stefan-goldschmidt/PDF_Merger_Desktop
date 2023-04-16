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
    mainModule.set("pdfmerger")
}

javafx {
    version = "20"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing")
}

tasks.test {
    useJUnitPlatform()
}

jlink {
    addExtraModulePath("javafx.controls,javafx.fxml")
    launcher {
        name = "PDFMerger Desktop"
        //jvmArgs.addAll(listOf("--add-exports=javafx.graphics"))
    }
    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerOptions.addAll(listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut"))
        }
    }
}

