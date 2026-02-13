package com.axondragonscale.qrqanvas.ui.creator

import com.axondragonscale.qrqanvas.model.ColorConfig
import com.axondragonscale.qrqanvas.model.DotStyle
import com.axondragonscale.qrqanvas.model.ErrorCorrectionLevel
import com.axondragonscale.qrqanvas.model.EyeStyle
import com.axondragonscale.qrqanvas.model.LogoShape
import com.axondragonscale.qrqanvas.model.QRStyle

/**
 * Created by Ronak Harkhani on 13/02/26
 */
sealed interface CreatorUiEvent {
    data class ContentTypeSelected(val contentType: ContentType) : CreatorUiEvent
    data class ContentInputChanged(val contentInput: ContentInputState) : CreatorUiEvent
    data class DotStyleSelected(val dotStyle: DotStyle) : CreatorUiEvent
    data class DotColorSelected(val color: ColorConfig) : CreatorUiEvent
    data class EyeStyleSelected(val eyeStyle: EyeStyle) : CreatorUiEvent
    data class EyeFrameColorSelected(val color: ColorConfig) : CreatorUiEvent
    data class EyeDotColorSelected(val color: ColorConfig) : CreatorUiEvent
    data class BackgroundColorSelected(val color: ColorConfig) : CreatorUiEvent
    data class LogoSelected(val uri: String?) : CreatorUiEvent
    data class LogoSizeChanged(val size: Float) : CreatorUiEvent
    data class LogoPaddingChanged(val padding: Float) : CreatorUiEvent
    data class LogoShapeSelected(val shape: LogoShape) : CreatorUiEvent
    data class ErrorCorrectionSelected(val level: ErrorCorrectionLevel) : CreatorUiEvent
    data class QuietZoneChanged(val quietZone: Int) : CreatorUiEvent
    data class TemplateSelected(val style: QRStyle) : CreatorUiEvent
    data object SaveClicked : CreatorUiEvent
    data object ShareClicked : CreatorUiEvent
    data object ExportClicked : CreatorUiEvent
    data object SaveAsTemplateClicked : CreatorUiEvent
    data class SaveTemplateNameChanged(val name: String) : CreatorUiEvent
    data object ConfirmSaveTemplate : CreatorUiEvent
    data object DismissSaveTemplate : CreatorUiEvent
}
