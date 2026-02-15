package com.axondragonscale.qrqanvas.ui.devtools

import com.axondragonscale.qrqanvas.database.entity.QRCodeEntity
import com.axondragonscale.qrqanvas.model.ColorData
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.QRErrorCorrection
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.model.WifiEncryption

/**
 * Created by Ronak Harkhani on 09/07/25
 */

/**
 * Generates test QR codes covering all 8 [QRContent] types with distinct styles:
 * Text, Url, WiFi, Contact, Email, Phone, Sms, Location.
 */
fun getTestQRCodes(): List<QRCodeEntity> {
    val now = System.currentTimeMillis()

    return listOf(
        // 1. Text — Classic black & white
        QRCodeEntity(
            title = "Hello World",
            content = QRContent.Text(value = "Hello, World! This is a test QR code."),
            style = QRStyle(
                dotStyle = DotStyle.Square,
                dotColor = ColorData.Solid(0xFF000000),
                eyeStyle = EyeStyle.Square,
                eyeFrameColor = ColorData.Solid(0xFF000000),
                eyeDotColor = ColorData.Solid(0xFF000000),
                backgroundColor = ColorData.Solid(0xFFFFFFFF),
                errorCorrection = QRErrorCorrection.M,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 2. Url — Blue gradient circles
        QRCodeEntity(
            title = "My Website",
            content = QRContent.Url(url = "https://example.com"),
            style = QRStyle(
                dotStyle = DotStyle.Circle,
                dotColor = ColorData.LinearGradient(
                    colors = listOf(0xFF1565C0, 0xFF42A5F5),
                    angleDegrees = 135f,
                ),
                eyeStyle = EyeStyle.Circle,
                eyeFrameColor = ColorData.Solid(0xFF1565C0),
                eyeDotColor = ColorData.Solid(0xFF42A5F5),
                backgroundColor = ColorData.Solid(0xFFFFFFFF),
                errorCorrection = QRErrorCorrection.Q,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 3. WiFi — Green rounded squares on light green
        QRCodeEntity(
            title = "Home Wi-Fi",
            content = QRContent.WiFi(
                ssid = "MyHomeNetwork",
                password = "supersecretpassword",
                encryptionType = WifiEncryption.WPA,
                hidden = false,
            ),
            style = QRStyle(
                dotStyle = DotStyle.RoundedSquare,
                dotColor = ColorData.Solid(0xFF2E7D32),
                eyeStyle = EyeStyle.RoundedSquare,
                eyeFrameColor = ColorData.Solid(0xFF1B5E20),
                eyeDotColor = ColorData.Solid(0xFF4CAF50),
                backgroundColor = ColorData.Solid(0xFFE8F5E9),
                errorCorrection = QRErrorCorrection.M,
                quietZone = 2,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 4. Contact — Purple radial gradient with leaf eyes
        QRCodeEntity(
            title = "John Doe",
            content = QRContent.Contact(
                name = "John Doe",
                phone = "+1-555-123-4567",
                email = "john.doe@example.com",
                organization = "Acme Corp",
                title = "Software Engineer",
                url = "https://johndoe.dev",
                address = "123 Main St, Springfield, IL",
                note = "Met at conference",
            ),
            style = QRStyle(
                dotStyle = DotStyle.Diamond,
                dotColor = ColorData.RadialGradient(
                    colors = listOf(0xFF7B1FA2, 0xFFCE93D8),
                ),
                eyeStyle = EyeStyle.Leaf,
                eyeFrameColor = ColorData.Solid(0xFF6A1B9A),
                eyeDotColor = ColorData.Solid(0xFFAB47BC),
                backgroundColor = ColorData.Solid(0xFFF3E5F5),
                errorCorrection = QRErrorCorrection.H,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 5. Email — Red-orange diagonal gradient with star dots
        QRCodeEntity(
            title = "Support Email",
            content = QRContent.Email(
                address = "support@example.com",
                subject = "Help Request",
                body = "Hi, I need assistance with...",
            ),
            style = QRStyle(
                dotStyle = DotStyle.Star,
                dotColor = ColorData.LinearGradient(
                    colors = listOf(0xFFD32F2F, 0xFFFF6D00),
                    angleDegrees = 45f,
                ),
                eyeStyle = EyeStyle.Diamond,
                eyeFrameColor = ColorData.Solid(0xFFD32F2F),
                eyeDotColor = ColorData.Solid(0xFFFF6D00),
                backgroundColor = ColorData.Solid(0xFFFFF8E1),
                errorCorrection = QRErrorCorrection.Q,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 6. Phone — Teal minimal circles, wide quiet zone
        QRCodeEntity(
            title = "Call Support",
            content = QRContent.Phone(number = "+1-800-555-0199"),
            style = QRStyle(
                dotStyle = DotStyle.Circle,
                dotColor = ColorData.Solid(0xFF00695C),
                eyeStyle = EyeStyle.Circle,
                eyeFrameColor = ColorData.Solid(0xFF00695C),
                eyeDotColor = ColorData.Solid(0xFF26A69A),
                backgroundColor = ColorData.Solid(0xFFE0F2F1),
                errorCorrection = QRErrorCorrection.L,
                quietZone = 3,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 7. Sms — Warm orange hearts with leaf eyes
        QRCodeEntity(
            title = "Send SMS",
            content = QRContent.Sms(
                number = "+1-555-987-6543",
                message = "Hey, scan my QR code!",
            ),
            style = QRStyle(
                dotStyle = DotStyle.Heart,
                dotColor = ColorData.LinearGradient(
                    colors = listOf(0xFFE65100, 0xFFFFAB00),
                    angleDegrees = 90f,
                ),
                eyeStyle = EyeStyle.Leaf,
                eyeFrameColor = ColorData.Solid(0xFFE65100),
                eyeDotColor = ColorData.Solid(0xFFFFAB00),
                backgroundColor = ColorData.Solid(0xFFFFF9C4),
                errorCorrection = QRErrorCorrection.M,
            ),
            createdAt = now,
            modifiedAt = now,
        ),

        // 8. Location — Dark mode: white on dark blue
        QRCodeEntity(
            title = "Central Park",
            content = QRContent.Location(
                latitude = 40.785091,
                longitude = -73.968285,
                label = "Central Park, New York",
            ),
            style = QRStyle(
                dotStyle = DotStyle.RoundedSquare,
                dotColor = ColorData.Solid(0xFFE8EAF6),
                eyeStyle = EyeStyle.RoundedSquare,
                eyeFrameColor = ColorData.Solid(0xFFFFFFFF),
                eyeDotColor = ColorData.Solid(0xFF9FA8DA),
                backgroundColor = ColorData.Solid(0xFF1A237E),
                errorCorrection = QRErrorCorrection.H,
                quietZone = 2,
            ),
            createdAt = now,
            modifiedAt = now,
        ),
    )
}
