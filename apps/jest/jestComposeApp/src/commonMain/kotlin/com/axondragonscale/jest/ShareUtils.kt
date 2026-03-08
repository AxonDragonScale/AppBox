package com.axondragonscale.jest

/**
 * Cross-platform sharing utility.
 *
 * Each platform provides its own actual implementation:
 * - Android: Intent.ACTION_SEND via Intent.createChooser
 * - Desktop: Copy to clipboard
 * - iOS: UIActivityViewController
 */
expect fun shareText(text: String)
