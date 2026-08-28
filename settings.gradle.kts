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
include(":foundation:resource")
include(":foundation:coroutines")
include(":foundation:logger:api")
include(":foundation:logger:impl")

include(":core:domain")
include(":core:data")
include(":core:analytics:api")
include(":core:analytics:impl")
include(":core:background:api")
include(":core:background:impl")
include(":core:connectivity:api")
include(":core:connectivity:impl")
include(":core:features:following:api")
include(":core:features:following:impl")
include(":uikit")
include(":navigation:core")
include(":navigation:swipe")
include(":feature:club:api")
include(":feature:club:impl")
include(":feature:matches:api")
include(":feature:matches:impl")
include(":feature:player:api")
include(":feature:player:impl")
include(":feature:squad:api")
include(":feature:squad:impl")
