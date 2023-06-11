plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "pdfmerger"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("org.apache.pdfbox:pdfbox:2.0.28")
    implementation("org.apache.pdfbox:preflight:2.0.28")
    implementation("org.kordamp.ikonli:ikonli-core:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-materialdesign2-pack:12.3.1")

}



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application() {
    mainClass.set("pdfmerger.Main")
    mainModule.set("pdfmerger")
}

javafx {
    version = "20"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing")
}

tasks.test {
    useJUnitPlatform()
    exclude("**/*")
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

