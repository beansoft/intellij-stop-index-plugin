repositories {
  // Fix for https://github.com/JetBrains/gradle-intellij-plugin/issues/520
  mavenCentral()
}

plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.13.3"
  id("org.jetbrains.kotlin.jvm") version "1.7.21"
}

intellij {
  pluginName.set("stop indexing")
  version.set("231.8770.65")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf(/* Plugin Dependencies */))

  instrumentCode.set(false)
  updateSinceUntilBuild.set(false)
}

tasks {
  publishPlugin {
    token.set(System.getenv("PLUGINS_JB_TOKEN"))
  }
}

// https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin-faq.html#how-to-disable-building-searchable-options
tasks.buildSearchableOptions {
  enabled = false
}