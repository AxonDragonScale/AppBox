package com.axondragonscale.cardy.emv

data class EmvTagInfo(
    val name: String,
    val description: String?,
)

/** Created by Ronak on 05/30/26 */
object EmvTags {
    private val registry: Map<String, EmvTagInfo> = buildMap {
        fun tag(hex: String, name: String, description: String) {
            put(hex, EmvTagInfo(name, description))
        }

        tag("4F", "AID", "Application Identifier (ADF Name)")
        tag("50", "Application Label", "Mnemonic associated with the AID")
        tag("57", "Track 2 Equivalent Data", "Contains PAN + expiry in mag stripe format")
        tag("5A", "Application PAN", "Primary Account Number")
        tag("5F20", "Cardholder Name", "Indicates cardholder name as per ISO 7813")
        tag("5F24", "Application Expiration Date", "Date after which application expires (YYMMDD)")
        tag("5F25", "Application Effective Date", "Date from which the application may be used (YYMMDD)")
        tag("5F28", "Issuer Country Code", "2-digit country code (ISO 3166)")
        tag("5F2A", "Transaction Currency Code", "Currency code (ISO 4217)")
        tag("5F2D", "Language Preference", "1-4 languages stored in order of preference")
        tag("5F34", "PAN Sequence Number", "Identifies and differentiates cards with the same PAN")
        tag("61", "Application Template", "Contains one or more data objects relevant to an application")
        tag("6F", "FCI Template", "File Control Information Template")
        tag("70", "EMV Proprietary Template", "Template proprietary to the EMV specification")
        tag("73", "Directory Discretionary Template", "Issuer discretionary part of the directory")
        tag("77", "Response Message Template Format 2", "Contains card response data")
        tag("80", "Response Message Template Format 1", "Contains primitive card response data")
        tag("82", "AIP", "Application Interchange Profile — card capabilities")
        tag("84", "DF Name", "Dedicated File name (AID of selected application)")
        tag("87", "Application Priority Indicator", "Indicates the priority of a given application")
        tag("88", "SFI of the Directory Elementary File", "Short File Identifier")
        tag("8C", "CDOL1", "Card Risk Management Data Object List 1")
        tag("8D", "CDOL2", "Card Risk Management Data Object List 2")
        tag("8E", "CVM List", "Cardholder Verification Method List")
        tag("8F", "Certification Authority Public Key Index", "Identifies CA's public key")
        tag("90", "Issuer Public Key Certificate", "Issued by the CA")
        tag("92", "Issuer Public Key Remainder", "Remaining part of Issuer Public Key")
        tag("93", "Signed Static Application Data", "Digital signature for offline auth")
        tag("94", "AFL", "Application File Locator — indicates files to read")
        tag("95", "TVR", "Terminal Verification Results")
        tag("9A", "Transaction Date", "Local date of transaction (YYMMDD)")
        tag("9C", "Transaction Type", "Type of financial transaction")
        tag("9F02", "Amount Authorized", "Authorized amount of the transaction")
        tag("9F03", "Amount Other", "Secondary amount associated with the transaction")
        tag("9F06", "AID (Terminal)", "Application Identifier as known to the terminal")
        tag("9F07", "Application Usage Control", "Indicates issuer's restrictions on usage")
        tag("9F08", "Application Version Number (Card)", "Version number assigned by payment system")
        tag("9F09", "Application Version Number (Terminal)", "Version number assigned by terminal")
        tag("9F0D", "Issuer Action Code - Default", "Specifies issuer actions for offline")
        tag("9F0E", "Issuer Action Code - Denial", "Specifies issuer actions for denial")
        tag("9F0F", "Issuer Action Code - Online", "Specifies issuer actions for online")
        tag("9F10", "Issuer Application Data", "Contains proprietary data from the issuer")
        tag("9F11", "Issuer Code Table Index", "Indicates code table for displaying Application Preferred Name")
        tag("9F12", "Application Preferred Name", "Preferred mnemonic associated with the AID")
        tag("9F1A", "Terminal Country Code", "Country code of the terminal")
        tag("9F1F", "Track 1 Discretionary Data", "Discretionary part of track 1")
        tag("9F26", "Application Cryptogram", "Cryptogram returned by the card")
        tag("9F27", "Cryptogram Information Data", "Indicates the type of cryptogram")
        tag("9F32", "Issuer Public Key Exponent", "Exponent for issuer's public key")
        tag("9F36", "Application Transaction Counter", "Counter maintained by the application")
        tag("9F37", "Unpredictable Number", "Value to provide variability")
        tag("9F38", "PDOL", "Processing Options Data Object List")
        tag("9F42", "Application Currency Code", "Currency in which the account is managed")
        tag("9F44", "Application Currency Exponent", "Indicates decimal point position")
        tag("9F46", "ICC Public Key Certificate", "Certificate for ICC public key")
        tag("9F47", "ICC Public Key Exponent", "Exponent for ICC's public key")
        tag("9F48", "ICC Public Key Remainder", "Remaining part of ICC public key")
        tag("9F4A", "Static Data Authentication Tag List", "Tags to be included in SDA")
        tag("9F4D", "Log Entry", "Identifies the transaction log file and number of records")
        tag("9F6E", "Contactless Form Factor Data", "Payment-system-specific contactless device and form factor information")
        tag("A5", "FCI Proprietary Template", "Identifies the data object proprietary to FCI")
        tag("BF0C", "FCI Issuer Discretionary Data", "Issuer discretionary part of FCI")
    }

    fun getTagInfo(tagHex: String): EmvTagInfo =
        registry[tagHex.uppercase()] ?: EmvTagInfo("Unknown", null)

    fun getTagName(tagHex: String): String =
        registry[tagHex.uppercase()]?.name ?: "Unknown (${tagHex.uppercase()})"
}
