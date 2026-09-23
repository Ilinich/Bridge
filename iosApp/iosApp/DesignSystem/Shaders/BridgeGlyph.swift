import SwiftUI

/// The four tab glyphs, drawn from the same coordinates as the Compose canvas.
///
/// SF Symbols came close but not close enough — its calendar carries a filled header and a grid of
/// dots this app never draws. These are the Compose paths, fraction for fraction, so the two tab
/// bars are the same drawing rather than two interpretations of one idea.
enum BridgeGlyph {
    case matchday, season, squad, club
}

struct BridgeGlyphView: View {

    let glyph: BridgeGlyph
    let tint: Color
    var size: CGFloat = 21

    var body: some View {
        Canvas { context, canvasSize in
            let width = canvasSize.width
            let height = canvasSize.height
            let line = min(width, height) * 0.09
            let style = StrokeStyle(lineWidth: line, lineCap: .round, lineJoin: .round)

            for path in paths(width: width, height: height) {
                context.stroke(path, with: .color(tint), style: style)
            }
        }
        .frame(width: size, height: size)
    }

    private func paths(width: CGFloat, height: CGFloat) -> [Path] {
        switch glyph {
        case .matchday: return [shield(width, height)]
        case .season: return calendar(width, height)
        case .squad: return people(width, height)
        case .club: return ground(width, height)
        }
    }

    private func shield(_ width: CGFloat, _ height: CGFloat) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: width * 0.5, y: height * 0.08))
        path.addLine(to: CGPoint(x: width * 0.88, y: height * 0.24))
        path.addLine(to: CGPoint(x: width * 0.88, y: height * 0.55))
        path.addCurve(
            to: CGPoint(x: width * 0.5, y: height * 0.95),
            control1: CGPoint(x: width * 0.88, y: height * 0.80),
            control2: CGPoint(x: width * 0.70, y: height * 0.90)
        )
        path.addCurve(
            to: CGPoint(x: width * 0.12, y: height * 0.55),
            control1: CGPoint(x: width * 0.30, y: height * 0.90),
            control2: CGPoint(x: width * 0.12, y: height * 0.80)
        )
        path.addLine(to: CGPoint(x: width * 0.12, y: height * 0.24))
        path.closeSubpath()
        return path
    }

    private func calendar(_ width: CGFloat, _ height: CGFloat) -> [Path] {
        let inset = width * 0.12
        var body = Path(
            roundedRect: CGRect(
                x: inset,
                y: height * 0.22,
                width: width - inset * 2,
                height: height * 0.66
            ),
            cornerRadius: width * 0.12
        )
        var rule = Path()
        rule.move(to: CGPoint(x: inset, y: height * 0.42))
        rule.addLine(to: CGPoint(x: width - inset, y: height * 0.42))
        body.addPath(rule)

        var hangers = Path()
        for fraction in [0.34, 0.66] {
            hangers.move(to: CGPoint(x: width * fraction, y: height * 0.08))
            hangers.addLine(to: CGPoint(x: width * fraction, y: height * 0.30))
        }
        return [body, hangers]
    }

    /// A pitch seen from above: centre circle and halfway line.
    private func ground(_ width: CGFloat, _ height: CGFloat) -> [Path] {
        let inset = width * 0.10
        var path = Path(
            roundedRect: CGRect(
                x: inset,
                y: inset,
                width: width - inset * 2,
                height: height - inset * 2
            ),
            cornerRadius: width * 0.10
        )
        path.move(to: CGPoint(x: width * 0.5, y: inset))
        path.addLine(to: CGPoint(x: width * 0.5, y: height - inset))
        path.addEllipse(
            in: CGRect(
                x: width * 0.5 - width * 0.16,
                y: height * 0.5 - width * 0.16,
                width: width * 0.32,
                height: width * 0.32
            )
        )
        return [path]
    }

    private func people(_ width: CGFloat, _ height: CGFloat) -> [Path] {
        var path = Path()
        path.addEllipse(
            in: CGRect(
                x: width * 0.38 - width * 0.17,
                y: height * 0.32 - width * 0.17,
                width: width * 0.34,
                height: width * 0.34
            )
        )
        path.move(to: CGPoint(x: width * 0.08, y: height * 0.92))
        path.addCurve(
            to: CGPoint(x: width * 0.68, y: height * 0.92),
            control1: CGPoint(x: width * 0.08, y: height * 0.62),
            control2: CGPoint(x: width * 0.68, y: height * 0.62)
        )
        path.addEllipse(
            in: CGRect(
                x: width * 0.76 - width * 0.13,
                y: height * 0.34 - width * 0.13,
                width: width * 0.26,
                height: width * 0.26
            )
        )
        path.move(to: CGPoint(x: width * 0.72, y: height * 0.62))
        path.addCurve(
            to: CGPoint(x: width * 0.94, y: height * 0.92),
            control1: CGPoint(x: width * 0.94, y: height * 0.66),
            control2: CGPoint(x: width * 0.94, y: height * 0.78)
        )
        return [path]
    }
}
