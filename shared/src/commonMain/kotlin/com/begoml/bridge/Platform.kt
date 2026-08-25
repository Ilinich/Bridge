package com.begoml.bridge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform