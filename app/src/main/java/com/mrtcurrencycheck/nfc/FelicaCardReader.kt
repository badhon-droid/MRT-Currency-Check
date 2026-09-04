package com.mrtcurrencycheck.nfc

import android.nfc.Tag
import android.nfc.tech.NfcF
import com.mrtcurrencycheck.model.CardReadResult
import com.mrtcurrencycheck.model.Transaction
import com.mrtcurrencycheck.nfc.parser.TransactionParser
import java.io.IOException

/**
 * Real reader for the Dhaka MRT Pass / Rapid Pass.
 *
 * These are Sony FeliCa cards, surfaced on Android as [NfcF]. The balance and
 * the last ~20 transactions sit on service 0x220F and can be read in the clear,
 * so no encryption keys or network calls are needed — everything is offline.
 */
class FelicaCardReader : CardReader {

    override fun supports(tag: Tag): Boolean = tag.techList.contains(NfcF::class.java.name)

    override fun read(tag: Tag): CardReadResult {
        val nfcF = NfcF.get(tag) ?: return CardReadResult.Unsupported
        val idm = tag.id // FeliCa IDm

        return try {
            nfcF.connect()
            // Give a slightly larger timeout so a quick tap doesn't get cut off.
            runCatching { nfcF.timeout = 1000 }

            val transactions = ArrayList<Transaction>(20)
            transactions += readChunk(nfcF, idm, startBlock = 0)
            transactions += readChunk(nfcF, idm, startBlock = 10)

            if (transactions.isEmpty()) {
                CardReadResult.Unsupported
            } else {
                // Block 0 is the most recent event, so its balance is the current one.
                val current = transactions.first().balance
                CardReadResult.Success(
                    balance = current,
                    cardIdHex = idm.joinToString("") { "%02X".format(it) },
                    transactions = transactions
                )
            }
        } catch (e: IOException) {
            CardReadResult.Error("Card moved too soon — hold it still and try again.")
        } catch (e: Exception) {
            CardReadResult.Error("Couldn't read this card.")
        } finally {
            runCatching { nfcF.close() }
        }
    }

    private fun readChunk(nfcF: NfcF, idm: ByteArray, startBlock: Int): List<Transaction> {
        val command = NfcCommandGenerator.readCommand(idm, startBlock = startBlock)
        val response = nfcF.transceive(command)
        return TransactionParser.parse(response)
    }
}
