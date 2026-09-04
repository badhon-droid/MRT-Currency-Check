package com.mrtcurrencycheck.model

import java.time.LocalDateTime

/**
 * A single trip / top-up entry decoded from one 16-byte FeliCa block.
 *
 * The MRT Pass / Rapid Pass stores a rolling history of the last ~20 events.
 * The most recent block (index 0) carries the current balance.
 */
data class Transaction(
    val timestamp: LocalDateTime,
    val fromStation: String,
    val toStation: String,
    /** Balance in BDT *after* this transaction was applied. */
    val balance: Int,
    val rawBlockHex: String
)
