package com.mrtcurrencycheck.model

/**
 * The outcome of a single card read. Returned by any [com.mrtcurrencycheck.nfc.CardReader].
 */
sealed interface CardReadResult {

    /** Card read successfully. [balance] is the current balance in BDT. */
    data class Success(
        val balance: Int,
        val cardIdHex: String,
        val transactions: List<Transaction>
    ) : CardReadResult

    /** A card was detected but it isn't a supported MRT/Rapid Pass (wrong tech/service). */
    data object Unsupported : CardReadResult

    /** Communication started but failed (card moved away, I/O error, etc.). */
    data class Error(val message: String) : CardReadResult
}
