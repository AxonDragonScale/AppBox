package com.axondragonscale.cardy.emv

/** Created by Ronak on 05/30/26 */
object EmvTags {

    data class TagInfo(
        val tagHex: String,
        val name: String,
        val description: String,
    )

    private val registry: Map<String, TagInfo> = buildMap {
        put("4F", TagInfo("4F", "AID", "Application Identifier (ADF Name)"))
        put("50", TagInfo("50", "Application Label", "Mnemonic associated with the AID"))
        put("57", TagInfo("57", "Track 2 Equivalent Data", "Contains PAN + expiry in mag stripe format"))
        put("5A", TagInfo("5A", "Application PAN", "Primary Account Number"))
        put("5F20", TagInfo("5F20", "Cardholder Name", "Indicates cardholder name as per ISO 7813"))
        put("5F24", TagInfo("5F24", "Application Expiration Date", "Date after which application expires (YYMMDD)"))
        put("5F25", TagInfo("5F25", "Application Effective Date", "Date from which the application may be used (YYMMDD)"))
        put("5F28", TagInfo("5F28", "Issuer Country Code", "2-digit country code (ISO 3166)"))
        put("5F2A", TagInfo("5F2A", "Transaction Currency Code", "Currency code (ISO 4217)"))
        put("5F2D", TagInfo("5F2D", "Language Preference", "1-4 languages stored in order of preference"))
        put("5F34", TagInfo("5F34", "PAN Sequence Number", "Identifies and differentiates cards with the same PAN"))
        put("61", TagInfo("61", "Application Template", "Contains one or more data objects relevant to an application"))
        put("6F", TagInfo("6F", "FCI Template", "File Control Information Template"))
        put("70", TagInfo("70", "EMV Proprietary Template", "Template proprietary to the EMV specification"))
        put("73", TagInfo("73", "Directory Discretionary Template", "Issuer discretionary part of the directory"))
        put("77", TagInfo("77", "Response Message Template Format 2", "Contains card response data"))
        put("80", TagInfo("80", "Response Message Template Format 1", "Contains primitive card response data"))
        put("82", TagInfo("82", "AIP", "Application Interchange Profile — card capabilities"))
        put("84", TagInfo("84", "DF Name", "Dedicated File name (AID of selected application)"))
        put("87", TagInfo("87", "Application Priority Indicator", "Indicates the priority of a given application"))
        put("88", TagInfo("88", "SFI of the Directory Elementary File", "Short File Identifier"))
        put("8C", TagInfo("8C", "CDOL1", "Card Risk Management Data Object List 1"))
        put("8D", TagInfo("8D", "CDOL2", "Card Risk Management Data Object List 2"))
        put("8E", TagInfo("8E", "CVM List", "Cardholder Verification Method List"))
        put("8F", TagInfo("8F", "Certification Authority Public Key Index", "Identifies CA's public key"))
        put("90", TagInfo("90", "Issuer Public Key Certificate", "Issued by the CA"))
        put("92", TagInfo("92", "Issuer Public Key Remainder", "Remaining part of Issuer Public Key"))
        put("93", TagInfo("93", "Signed Static Application Data", "Digital signature for offline auth"))
        put("94", TagInfo("94", "AFL", "Application File Locator — indicates files to read"))
        put("95", TagInfo("95", "TVR", "Terminal Verification Results"))
        put("9A", TagInfo("9A", "Transaction Date", "Local date of transaction (YYMMDD)"))
        put("9C", TagInfo("9C", "Transaction Type", "Type of financial transaction"))
        put("9F02", TagInfo("9F02", "Amount Authorized", "Authorized amount of the transaction"))
        put("9F03", TagInfo("9F03", "Amount Other", "Secondary amount associated with the transaction"))
        put("9F06", TagInfo("9F06", "AID (Terminal)", "Application Identifier as known to the terminal"))
        put("9F07", TagInfo("9F07", "Application Usage Control", "Indicates issuer's restrictions on usage"))
        put("9F08", TagInfo("9F08", "Application Version Number (Card)", "Version number assigned by payment system"))
        put("9F09", TagInfo("9F09", "Application Version Number (Terminal)", "Version number assigned by terminal"))
        put("9F0D", TagInfo("9F0D", "Issuer Action Code - Default", "Specifies issuer actions for offline"))
        put("9F0E", TagInfo("9F0E", "Issuer Action Code - Denial", "Specifies issuer actions for denial"))
        put("9F0F", TagInfo("9F0F", "Issuer Action Code - Online", "Specifies issuer actions for online"))
        put("9F10", TagInfo("9F10", "Issuer Application Data", "Contains proprietary data from the issuer"))
        put("9F11", TagInfo("9F11", "Issuer Code Table Index", "Indicates code table for displaying Application Preferred Name"))
        put("9F12", TagInfo("9F12", "Application Preferred Name", "Preferred mnemonic associated with the AID"))
        put("9F1A", TagInfo("9F1A", "Terminal Country Code", "Country code of the terminal"))
        put("9F1F", TagInfo("9F1F", "Track 1 Discretionary Data", "Discretionary part of track 1"))
        put("9F26", TagInfo("9F26", "Application Cryptogram", "Cryptogram returned by the card"))
        put("9F27", TagInfo("9F27", "Cryptogram Information Data", "Indicates the type of cryptogram"))
        put("9F32", TagInfo("9F32", "Issuer Public Key Exponent", "Exponent for issuer's public key"))
        put("9F36", TagInfo("9F36", "Application Transaction Counter", "Counter maintained by the application"))
        put("9F37", TagInfo("9F37", "Unpredictable Number", "Value to provide variability"))
        put("9F38", TagInfo("9F38", "PDOL", "Processing Options Data Object List"))
        put("9F42", TagInfo("9F42", "Application Currency Code", "Currency in which the account is managed"))
        put("9F44", TagInfo("9F44", "Application Currency Exponent", "Indicates decimal point position"))
        put("9F46", TagInfo("9F46", "ICC Public Key Certificate", "Certificate for ICC public key"))
        put("9F47", TagInfo("9F47", "ICC Public Key Exponent", "Exponent for ICC's public key"))
        put("9F48", TagInfo("9F48", "ICC Public Key Remainder", "Remaining part of ICC public key"))
        put("9F4A", TagInfo("9F4A", "Static Data Authentication Tag List", "Tags to be included in SDA"))
        put("9F4D", TagInfo("9F4D", "Log Entry", "Identifies the transaction log file and number of records"))
        put("9F6E", TagInfo("9F6E", "Contactless Form Factor Data", "Payment-system-specific contactless device and form factor information"))
        put("A5", TagInfo("A5", "FCI Proprietary Template", "Identifies the data object proprietary to FCI"))
        put("BF0C", TagInfo("BF0C", "FCI Issuer Discretionary Data", "Issuer discretionary part of FCI"))
    }

    fun getTagInfo(tagHex: String): TagInfo? = registry[tagHex.uppercase()]

    fun getTagName(tagHex: String): String =
        registry[tagHex.uppercase()]?.name ?: "Unknown (${tagHex.uppercase()})"
}
