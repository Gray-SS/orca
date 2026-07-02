rootProject.name = "orca"

plugins {
   // id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include(":modules:cli")
include(":modules:compiler:core")
include(":modules:lsp")
include(":modules:plugins:gradle")
include(":modules:stdlib")
