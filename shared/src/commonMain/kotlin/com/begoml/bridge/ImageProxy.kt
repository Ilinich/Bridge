package com.begoml.bridge

import coil3.ImageLoader

/**
 * Whatever the platform has to do to its image loader before it can fetch.
 *
 * Nothing, on every platform that is not a browser.
 */
internal expect fun ImageLoader.Builder.platformImages(): ImageLoader.Builder
