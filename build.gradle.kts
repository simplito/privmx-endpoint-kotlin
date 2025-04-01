import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.AbstractDokkaTask

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    id("org.jetbrains.dokka") version "2.0.0"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

subprojects{
    apply(plugin = "org.jetbrains.dokka")
    val dokkaTaskConfiguration: AbstractDokkaTask.() -> Unit = {
        outputDirectory.set(file(layout.buildDirectory.file("customHtml")))
//        val indexFile = outputDirectory.file("index.html").get().asFile
//        val svgFile = outputDirectory.file("ui-kit/assets/theme-toggle.svg").get().asFile
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            templatesDir = file(rootProject.layout.projectDirectory.file("docs/templates"))
            customAssets = listOf(
                rootProject.layout.projectDirectory.file("docs/fonts/Manrope-VariableFont_wght.ttf").asFile
            )
            customStyleSheets = listOf(
                rootProject.layout.projectDirectory.file("docs/styles/style.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/main.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/font-manrope-auto.css").asFile
            )
        }
        doLast {
            copy {
                from(rootProject.layout.projectDirectory.file("docs/fonts"))
                into(outputDirectory.file("fonts"))
            }
        }
//        doLast {
//            val content = svgFile.readText().replace(Regex("rgba\\([^\\)]*\\)"), "currentColor")
//            val buttonTag =
//                "<button class=\"navigation-controls--btn custom-header-icons\" id=\"theme-toggle-button\" type=\"button\">$content</button>"
//            indexFile.readText(Charsets.UTF_8).replace(
//                Regex(
//                    "<button class=\"navigation-controls--btn navigation-controls--btn_theme\"[^<]*</button>",
//                    RegexOption.MULTILINE
//                ), buttonTag
//            ).let {
//                indexFile.writeText(it)
//            }
//        }
    }
    tasks.register<DokkaTask>("customHtml"){
        dokkaTaskConfiguration()
    }
    tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets{
            configureEach {
                documentedVisibilities.set(setOf(
                    Visibility.PUBLIC,
                    Visibility.PROTECTED
                ))
            }
        }
    }

}