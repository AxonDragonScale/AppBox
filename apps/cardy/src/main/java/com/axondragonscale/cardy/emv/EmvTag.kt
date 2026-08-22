package com.axondragonscale.cardy.emv

sealed interface IEmvTag {
    val hex: String
    val displayName: String
    val description: String?
}

enum class EmvTag(
    override val hex: String,
    override val displayName: String,
    override val description: String,
) : IEmvTag {
    AID("4F", "AID", "Application Identifier (ADF Name)"),
    APPLICATION_LABEL("50", "Application Label", "Mnemonic associated with the AID"),
    TRACK_2_EQUIVALENT_DATA("57", "Track 2 Equivalent Data", "Contains PAN + expiry in mag stripe format"),
    APPLICATION_PAN("5A", "Application PAN", "Primary Account Number"),
    CARDHOLDER_NAME("5F20", "Cardholder Name", "Indicates cardholder name as per ISO 7813"),
    APPLICATION_EXPIRATION_DATE("5F24", "Application Expiration Date", "Date after which application expires (YYMMDD)"),
    APPLICATION_EFFECTIVE_DATE("5F25", "Application Effective Date", "Date from which the application may be used (YYMMDD)"),
    ISSUER_COUNTRY_CODE("5F28", "Issuer Country Code", "2-digit country code (ISO 3166)"),
    TRANSACTION_CURRENCY_CODE("5F2A", "Transaction Currency Code", "Currency code (ISO 4217)"),
    LANGUAGE_PREFERENCE("5F2D", "Language Preference", "1-4 languages stored in order of preference"),
    PAN_SEQUENCE_NUMBER("5F34", "PAN Sequence Number", "Identifies and differentiates cards with the same PAN"),
    APPLICATION_TEMPLATE("61", "Application Template", "Contains one or more data objects relevant to an application"),
    FCI_TEMPLATE("6F", "FCI Template", "File Control Information Template"),
    EMV_PROPRIETARY_TEMPLATE("70", "EMV Proprietary Template", "Template proprietary to the EMV specification"),
    DIRECTORY_DISCRETIONARY_TEMPLATE("73", "Directory Discretionary Template", "Issuer discretionary part of the directory"),
    RESPONSE_MESSAGE_TEMPLATE_2("77", "Response Message Template Format 2", "Contains card response data"),
    RESPONSE_MESSAGE_TEMPLATE_1("80", "Response Message Template Format 1", "Contains primitive card response data"),
    AIP("82", "AIP", "Application Interchange Profile — card capabilities"),
    DF_NAME("84", "DF Name", "Dedicated File name (AID of selected application)"),
    APPLICATION_PRIORITY_INDICATOR("87", "Application Priority Indicator", "Indicates the priority of a given application"),
    DIRECTORY_SFI("88", "SFI of the Directory Elementary File", "Short File Identifier"),
    CDOL1("8C", "CDOL1", "Card Risk Management Data Object List 1"),
    CDOL2("8D", "CDOL2", "Card Risk Management Data Object List 2"),
    CVM_LIST("8E", "CVM List", "Cardholder Verification Method List"),
    CA_PUBLIC_KEY_INDEX("8F", "Certification Authority Public Key Index", "Identifies CA's public key"),
    ISSUER_PUBLIC_KEY_CERTIFICATE("90", "Issuer Public Key Certificate", "Issued by the CA"),
    ISSUER_PUBLIC_KEY_REMAINDER("92", "Issuer Public Key Remainder", "Remaining part of Issuer Public Key"),
    SIGNED_STATIC_APPLICATION_DATA("93", "Signed Static Application Data", "Digital signature for offline auth"),
    AFL("94", "AFL", "Application File Locator — indicates files to read"),
    TVR("95", "TVR", "Terminal Verification Results"),
    TRANSACTION_DATE("9A", "Transaction Date", "Local date of transaction (YYMMDD)"),
    TRANSACTION_TYPE("9C", "Transaction Type", "Type of financial transaction"),
    AMOUNT_AUTHORIZED("9F02", "Amount Authorized", "Authorized amount of the transaction"),
    AMOUNT_OTHER("9F03", "Amount Other", "Secondary amount associated with the transaction"),
    TERMINAL_AID("9F06", "AID (Terminal)", "Application Identifier as known to the terminal"),
    APPLICATION_USAGE_CONTROL("9F07", "Application Usage Control", "Indicates issuer's restrictions on usage"),
    CARD_APPLICATION_VERSION("9F08", "Application Version Number (Card)", "Version number assigned by payment system"),
    TERMINAL_APPLICATION_VERSION("9F09", "Application Version Number (Terminal)", "Version number assigned by terminal"),
    ISSUER_ACTION_CODE_DEFAULT("9F0D", "Issuer Action Code - Default", "Specifies issuer actions for offline"),
    ISSUER_ACTION_CODE_DENIAL("9F0E", "Issuer Action Code - Denial", "Specifies issuer actions for denial"),
    ISSUER_ACTION_CODE_ONLINE("9F0F", "Issuer Action Code - Online", "Specifies issuer actions for online"),
    ISSUER_APPLICATION_DATA("9F10", "Issuer Application Data", "Contains proprietary data from the issuer"),
    ISSUER_CODE_TABLE_INDEX("9F11", "Issuer Code Table Index", "Indicates code table for displaying Application Preferred Name"),
    APPLICATION_PREFERRED_NAME("9F12", "Application Preferred Name", "Preferred mnemonic associated with the AID"),
    TERMINAL_COUNTRY_CODE("9F1A", "Terminal Country Code", "Country code of the terminal"),
    TRACK_1_DISCRETIONARY_DATA("9F1F", "Track 1 Discretionary Data", "Discretionary part of track 1"),
    APPLICATION_CRYPTOGRAM("9F26", "Application Cryptogram", "Cryptogram returned by the card"),
    CRYPTOGRAM_INFORMATION_DATA("9F27", "Cryptogram Information Data", "Indicates the type of cryptogram"),
    ISSUER_PUBLIC_KEY_EXPONENT("9F32", "Issuer Public Key Exponent", "Exponent for issuer's public key"),
    TERMINAL_CAPABILITIES("9F33", "Terminal Capabilities", "Indicates the card data input, CVM, and security capabilities of the terminal"),
    CVM_RESULTS("9F34", "CVM Results", "Indicates the results of the last cardholder verification performed"),
    TERMINAL_TYPE("9F35", "Terminal Type", "Indicates the environment and communication capabilities of the terminal"),
    APPLICATION_TRANSACTION_COUNTER("9F36", "Application Transaction Counter", "Counter maintained by the application"),
    UNPREDICTABLE_NUMBER("9F37", "Unpredictable Number", "Value to provide variability"),
    PDOL("9F38", "PDOL", "Processing Options Data Object List"),
    APPLICATION_CURRENCY_CODE("9F42", "Application Currency Code", "Currency in which the account is managed"),
    APPLICATION_CURRENCY_EXPONENT("9F44", "Application Currency Exponent", "Indicates decimal point position"),
    ICC_PUBLIC_KEY_CERTIFICATE("9F46", "ICC Public Key Certificate", "Certificate for ICC public key"),
    ICC_PUBLIC_KEY_EXPONENT("9F47", "ICC Public Key Exponent", "Exponent for ICC's public key"),
    ICC_PUBLIC_KEY_REMAINDER("9F48", "ICC Public Key Remainder", "Remaining part of ICC Public Key"),
    SDA_TAG_LIST("9F4A", "Static Data Authentication Tag List", "Tags to be included in SDA"),
    LOG_ENTRY("9F4D", "Log Entry", "Identifies the transaction log file and number of records"),
    TERMINAL_TRANSACTION_QUALIFIERS("9F66", "Terminal Transaction Qualifiers", "Indicates terminal requirements and contactless capabilities"),
    CONTACTLESS_FORM_FACTOR_DATA("9F6E", "Contactless Form Factor Data", "Payment-system-specific contactless device and form factor information"),
    FCI_PROPRIETARY_TEMPLATE("A5", "FCI Proprietary Template", "Identifies the data object proprietary to FCI"),
    FCI_ISSUER_DISCRETIONARY_DATA("BF0C", "FCI Issuer Discretionary Data", "Issuer discretionary part of FCI");

    companion object {
        private val byHex = entries.associateBy(EmvTag::hex)

        fun fromHex(hex: String): EmvTag? = byHex[hex.uppercase()]

        fun resolve(hex: String): IEmvTag {
            val normalized = hex.uppercase()
            return byHex[normalized] ?: UnknownEmvTag(normalized)
        }
    }
}

data class UnknownEmvTag(
    override val hex: String,
) : IEmvTag {
    override val displayName = "Unknown"
    override val description: String? = null
}
