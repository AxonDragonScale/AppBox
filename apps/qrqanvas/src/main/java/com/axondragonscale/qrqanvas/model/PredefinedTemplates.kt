package com.axondragonscale.qrqanvas.model

/**
 * Created by Ronak Harkhani on 09/07/25
 */
object PredefinedTemplates {

    private val Classic = Template(
        id = "predefined_classic",
        name = "Classic",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Square,
            dotColor = ColorData.Solid(0xFF000000),
            eyeStyle = EyeStyle.Square,
            eyeFrameColor = ColorData.Solid(0xFF000000),
            eyeDotColor = ColorData.Solid(0xFF000000),
            backgroundColor = ColorData.Solid(0xFFFFFFFF),
        ),
    )

    private val Rounded = Template(
        id = "predefined_rounded",
        name = "Rounded",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorData.Solid(0xFF000000),
            eyeStyle = EyeStyle.RoundedSquare,
            eyeFrameColor = ColorData.Solid(0xFF000000),
            eyeDotColor = ColorData.Solid(0xFF000000),
            backgroundColor = ColorData.Solid(0xFFFFFFFF),
        ),
    )

    private val Neon = Template(
        id = "predefined_neon",
        name = "Neon",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorData.LinearGradient(
                colors = listOf(0xFF00E5FF, 0xFFFF00FF),
                angleDegrees = 135f,
            ),
            eyeStyle = EyeStyle.Circle,
            eyeFrameColor = ColorData.Solid(0xFF00E5FF),
            eyeDotColor = ColorData.Solid(0xFFFF00FF),
            backgroundColor = ColorData.Solid(0xFF1A1A2E),
        ),
    )

    private val Elegant = Template(
        id = "predefined_elegant",
        name = "Elegant",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.RoundedSquare,
            dotColor = ColorData.Solid(0xFF0D1B2A),
            eyeStyle = EyeStyle.Leaf,
            eyeFrameColor = ColorData.Solid(0xFF0D1B2A),
            eyeDotColor = ColorData.Solid(0xFF1B3A5C),
            backgroundColor = ColorData.Solid(0xFFF5F0E8),
        ),
    )

    private val Sunset = Template(
        id = "predefined_sunset",
        name = "Sunset",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Diamond,
            dotColor = ColorData.RadialGradient(
                colors = listOf(0xFFFF6B35, 0xFFD32F2F),
                centerX = 0.5f,
                centerY = 0.5f,
            ),
            eyeStyle = EyeStyle.Diamond,
            eyeFrameColor = ColorData.Solid(0xFFFF6B35),
            eyeDotColor = ColorData.Solid(0xFFD32F2F),
            backgroundColor = ColorData.Solid(0xFFFFF8E1),
        ),
    )

    private val Monochrome = Template(
        id = "predefined_monochrome",
        name = "Monochrome",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Square,
            dotColor = ColorData.Solid(0xFF424242),
            eyeStyle = EyeStyle.Square,
            eyeFrameColor = ColorData.Solid(0xFF424242),
            eyeDotColor = ColorData.Solid(0xFF616161),
            backgroundColor = ColorData.Solid(0xFFE0E0E0),
        ),
    )

    private val Ocean = Template(
        id = "predefined_ocean",
        name = "Ocean",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorData.LinearGradient(
                colors = listOf(0xFF009688, 0xFF1565C0),
                angleDegrees = 90f,
            ),
            eyeStyle = EyeStyle.Circle,
            eyeFrameColor = ColorData.Solid(0xFF009688),
            eyeDotColor = ColorData.Solid(0xFF1565C0),
            backgroundColor = ColorData.Solid(0xFFFFFFFF),
        ),
    )

    private val Midnight = Template(
        id = "predefined_midnight",
        name = "Midnight",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.RoundedSquare,
            dotColor = ColorData.Solid(0xFFFFFFFF),
            eyeStyle = EyeStyle.RoundedSquare,
            eyeFrameColor = ColorData.Solid(0xFFFFFFFF),
            eyeDotColor = ColorData.Solid(0xFFE0E0E0),
            backgroundColor = ColorData.Solid(0xFF000000),
        ),
    )

    val all: List<Template> = listOf(
        Classic,
        Rounded,
        Neon,
        Elegant,
        Sunset,
        Monochrome,
        Ocean,
        Midnight,
    )
}
