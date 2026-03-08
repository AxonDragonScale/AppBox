package com.axondragonscale.jest

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun shareText(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}
