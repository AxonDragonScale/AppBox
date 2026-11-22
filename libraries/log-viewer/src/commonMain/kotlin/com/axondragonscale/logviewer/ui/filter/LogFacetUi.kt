package com.axondragonscale.logviewer.ui.filter

import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.source.LogRule

/** Created by Ronak Harkhani on 11/22/26 */

/** The words the filter UI uses for a facet, kept beside the components that draw them. */

internal val LogFacet.noun: String
    get() = when (this) {
        LogFacet.TAG -> "tag"
        LogFacet.PACKAGE -> "package"
    }

internal val LogFacet.sectionTitle: String get() = noun.replaceFirstChar { it.uppercase() }

internal val LogFacet.placeholder: String get() = "$sectionTitle name"

internal val LogFacet.emptyHint: String
    get() = when (this) {
        LogFacet.TAG -> "No tag rules. Include keeps a tag's lines; exclude drops them."
        LogFacet.PACKAGE -> "No package rules. A package follows the app's live PIDs."
    }

internal val LogRule.chipLabel: String
    get() = when (facet) {
        LogFacet.TAG -> value
        LogFacet.PACKAGE -> "pkg:$value"
    }

internal fun LogFacet.isSupportedBy(capabilities: LogCapabilities): Boolean = when (this) {
    LogFacet.TAG -> true
    LogFacet.PACKAGE -> capabilities.packageName
}
