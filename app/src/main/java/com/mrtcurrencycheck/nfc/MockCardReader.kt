package com.mrtcurrencycheck.nfc

import android.nfc.Tag
import com.mrtcurrencycheck.model.CardReadResult
import com.mrtcurrencycheck.model.Transaction
import java.time.LocalDateTime

/**
 * Deterministic fake reader for development and demos.
 *
 * The Android emulator has no real NFC hardware, and not everyone has a card on
 * hand while building the UI. Swap [FelicaCardReader] for this in MainActivity
 * to exercise the full success path with sample data.
 */
class MockCardReader : CardReader {

    override fun supports(tag: Tag): Boolean = true

    override fun read(tag: Tag): CardReadResult {
        Thread.sleep(450) // mimic the real read latency so the loading state is visible
        val now = LocalDateTime.now()
        val sample = listOf(
            Transaction(now, "Agargaon", "Motijheel", balance = 372, rawBlockHex = "MOCK"),
            Transaction(now.minusDays(1), "Motijheel", "Agargaon", balance = 472, rawBlockHex = "MOCK"),
            Transaction(now.minusDays(2), "Top-up", "—", balance = 572, rawBlockHex = "MOCK")
        )
        return CardReadResult.Success(
            balance = sample.first().balance,
            cardIdHex = "0102030405060708",
            transactions = sample
        )
    }
}
