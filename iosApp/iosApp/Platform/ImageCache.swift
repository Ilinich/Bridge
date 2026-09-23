import Foundation

/// Where the badges and cutouts live between screens.
///
/// AsyncImage loads through URLSession.shared, which caches only what the shared URLCache is sized
/// to hold — 512 KB of memory by default, which is less than one squad photo. Coil on the Android
/// side keeps a memory and a disk cache; this is the same intent with the tools iOS already has,
/// and it respects the servers' own cache headers rather than inventing a policy.
enum ImageCache {

    private static let memory = 64 * 1024 * 1024
    private static let disk = 256 * 1024 * 1024

    static func configure() {
        URLCache.shared = URLCache(memoryCapacity: memory, diskCapacity: disk)
    }
}
