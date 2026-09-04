package com.mrtcurrencycheck.model

/**
 * Everything the single screen needs to render. The ViewModel exposes one of these.
 */
sealed interface ScanState {

    /** NFC ready, waiting for a tap. */
    data object WaitingForTap : ScanState

    /** A tag was detected; reading in progress. Shown as a brief loading state. */
    data object Reading : ScanState

    /** Balance read successfully. */
    data class Balance(val result: CardReadResult.Success) : ScanState

    /** Something went wrong, or an unsupported card was tapped. */
    data class Error(val message: String) : ScanState

    /** Device has no NFC, or NFC is turned off in system settings. */
    data class NfcUnavailable(val reason: String) : ScanState
}
