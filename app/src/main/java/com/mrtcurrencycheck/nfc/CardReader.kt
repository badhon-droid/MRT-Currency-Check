package com.mrtcurrencycheck.nfc

import android.nfc.Tag
import com.mrtcurrencycheck.model.CardReadResult

/**
 * Pluggable card-reader abstraction.
 *
 * The app talks to cards only through this interface, so the actual transport
 * (FeliCa today, an official DMTCL API tomorrow, or a mock for demos) can be
 * swapped without touching the ViewModel or UI.
 *
 *   - [FelicaCardReader]  -> real Dhaka MRT Pass / Rapid Pass over NFC-F
 *   - [MockCardReader]    -> deterministic fake data for UI work / emulator
 *
 * To add an official API integration later, implement this interface and
 * inject it in [com.mrtcurrencycheck.MainActivity].
 */
interface CardReader {

    /** True if this reader can handle the given tag (checks supported tech). */
    fun supports(tag: Tag): Boolean

    /** Read the card. Must be called off the main thread. Never throws. */
    fun read(tag: Tag): CardReadResult
}
