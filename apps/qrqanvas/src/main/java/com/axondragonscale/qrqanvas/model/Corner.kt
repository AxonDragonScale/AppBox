package com.axondragonscale.qrqanvas.model

/**
 * Created by Ronak Harkhani on 10/07/25
 *
 * Represents a corner position. Used to identify which corner a QR finder
 * pattern occupies so that asymmetric eye styles (e.g. Leaf) can orient
 * themselves to point outward.
 */
enum class Corner {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}
