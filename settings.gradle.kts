rootProject.name = "Bridge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":detekt-rules")
include(":androidApp")
include(":benchmark")
include(":shared")

include(":foundation:tessera")
include(":foundation:cache")
include(":foundation:logger:api")
include(":foundation:logger:impl")
include(":foundation:analytics:api")
include(":foundation:analytics:impl")
include(":core:data")
include(":uikit")
include(":navigation:core")
include(":navigation:swipe")
include(":feature:club:api")
include(":feature:club:impl")
include(":feature:matches:api")
include(":feature:matches:impl")
include(":feature:squad:api")
include(":feature:squad:impl")
