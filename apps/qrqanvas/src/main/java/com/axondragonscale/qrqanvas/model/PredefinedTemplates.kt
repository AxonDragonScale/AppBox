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
            dotColor = ColorConfig.Solid(0xFF000000),
            eyeStyle = EyeStyle.Square,
            eyeFrameColor = ColorConfig.Solid(0xFF000000),
            eyeDotColor = ColorConfig.Solid(0xFF000000),
            backgroundColor = ColorConfig.Solid(0xFFFFFFFF),
        ),
    )

    private val Rounded = Template(
        id = "predefined_rounded",
        name = "Rounded",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorConfig.Solid(0xFF000000),
            eyeStyle = EyeStyle.RoundedSquare,
            eyeFrameColor = ColorConfig.Solid(0xFF000000),
            eyeDotColor = ColorConfig.Solid(0xFF000000),
            backgroundColor = ColorConfig.Solid(0xFFFFFFFF),
        ),
    )

    private val Neon = Template(
        id = "predefined_neon",
        name = "Neon",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorConfig.LinearGradient(
                colors = listOf(0xFF00E5FF, 0xFFFF00FF),
                angleDegrees = 135f,
            ),
            eyeStyle = EyeStyle.Circle,
            eyeFrameColor = ColorConfig.Solid(0xFF00E5FF),
            eyeDotColor = ColorConfig.Solid(0xFFFF00FF),
            backgroundColor = ColorConfig.Solid(0xFF1A1A2E),
        ),
    )

    private val Elegant = Template(
        id = "predefined_elegant",
        name = "Elegant",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.RoundedSquare,
            dotColor = ColorConfig.Solid(0xFF0D1B2A),
            eyeStyle = EyeStyle.Leaf,
            eyeFrameColor = ColorConfig.Solid(0xFF0D1B2A),
            eyeDotColor = ColorConfig.Solid(0xFF1B3A5C),
            backgroundColor = ColorConfig.Solid(0xFFF5F0E8),
        ),
    )

    private val Sunset = Template(
        id = "predefined_sunset",
        name = "Sunset",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Diamond,
            dotColor = ColorConfig.RadialGradient(
                colors = listOf(0xFFFF6B35, 0xFFD32F2F),
                centerX = 0.5f,
                centerY = 0.5f,
            ),
            eyeStyle = EyeStyle.Diamond,
            eyeFrameColor = ColorConfig.Solid(0xFFFF6B35),
            eyeDotColor = ColorConfig.Solid(0xFFD32F2F),
            backgroundColor = ColorConfig.Solid(0xFFFFF8E1),
        ),
    )

    private val Monochrome = Template(
        id = "predefined_monochrome",
        name = "Monochrome",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Square,
            dotColor = ColorConfig.Solid(0xFF424242),
            eyeStyle = EyeStyle.Square,
            eyeFrameColor = ColorConfig.Solid(0xFF424242),
            eyeDotColor = ColorConfig.Solid(0xFF616161),
            backgroundColor = ColorConfig.Solid(0xFFE0E0E0),
        ),
    )

    private val Ocean = Template(
        id = "predefined_ocean",
        name = "Ocean",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.Circle,
            dotColor = ColorConfig.LinearGradient(
                colors = listOf(0xFF009688, 0xFF1565C0),
                angleDegrees = 90f,
            ),
            eyeStyle = EyeStyle.Circle,
            eyeFrameColor = ColorConfig.Solid(0xFF009688),
            eyeDotColor = ColorConfig.Solid(0xFF1565C0),
            backgroundColor = ColorConfig.Solid(0xFFFFFFFF),
        ),
    )

    private val Midnight = Template(
        id = "predefined_midnight",
        name = "Midnight",
        isPredefined = true,
        style = QRStyle(
            dotStyle = DotStyle.RoundedSquare,
            dotColor = ColorConfig.Solid(0xFFFFFFFF),
            eyeStyle = EyeStyle.RoundedSquare,
            eyeFrameColor = ColorConfig.Solid(0xFFFFFFFF),
            eyeDotColor = ColorConfig.Solid(0xFFE0E0E0),
            backgroundColor = ColorConfig.Solid(0xFF000000),
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
