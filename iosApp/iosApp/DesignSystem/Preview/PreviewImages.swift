import SwiftUI

private struct BundledImagesKey: EnvironmentKey {
    static let defaultValue = false
}

extension EnvironmentValues {

    /// Whether the images on this screen come from the bundle rather than from the network.
    ///
    /// Only `PreviewHost` turns this on. A canvas that fetches is a canvas that shows a spinner on
    /// a train and a photograph at a desk, and neither tells you whether the layout is right; the
    /// three pictures under `Preview Content` are stripped from a release build, so this costs the
    /// app nothing.
    var usesBundledImages: Bool {
        get { self[BundledImagesKey.self] }
        set { self[BundledImagesKey.self] = newValue }
    }
}
