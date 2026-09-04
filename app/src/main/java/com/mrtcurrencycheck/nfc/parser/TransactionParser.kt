package com.mrtcurrencycheck.nfc.parser

import com.mrtcurrencycheck.model.Transaction
import java.time.LocalDateTime

/**
 * Parses the raw response of a FeliCa Read Without Encryption command into a
 * list of [Transaction]s.
 *
 * Response framing:
 *   [0]      response length
 *   [1]      response code (0x07)
 *   [2..9]   IDm
 *   [10]     status flag 1   (0x00 = OK)
 *   [11]     status flag 2   (0x00 = OK)
 *   [12]     number of blocks returned
 *   [13..]   block data, 16 bytes per block
 *
 * 16-byte block layout (relevant fields):
 *   [4..6]   packed timestamp (big-endian 24-bit)
 *   [8]      from-station code
 *   [10]     to-station code
 *   [11..13] balance in BDT (little-endian 24-bit)
 */
internal object TransactionParser {

    private const val BLOCK_SIZE = 16
    private val CUTOFF: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)

    fun parse(response: ByteArray): List<Transaction> {
        if (response.size < 13) return emptyList()
        if (response[10] != 0x00.toByte() || response[11] != 0x00.toByte()) return emptyList()

        val numBlocks = ByteParser.byteAt(response, 12)
        val data = response.copyOfRange(13, response.size)
        if (data.size < numBlocks * BLOCK_SIZE) return emptyList()

        val out = ArrayList<Transaction>(numBlocks)
        for (i in 0 until numBlocks) {
            val block = data.copyOfRange(i * BLOCK_SIZE, i * BLOCK_SIZE + BLOCK_SIZE)
            val tx = parseBlock(block)
            // Empty/uninitialised slots decode to junk dates; filter them out.
            if (tx.timestamp.isAfter(CUTOFF)) out.add(tx)
        }
        return out
    }

    private fun parseBlock(block: ByteArray): Transaction {
        val timestamp = TimestampService.decode(ByteParser.int24BE(block, 4))
        val from = StationService.name(ByteParser.byteAt(block, 8))
        val to = StationService.name(ByteParser.byteAt(block, 10))
        val balance = ByteParser.int24LE(block, 11)
        return Transaction(
            timestamp = timestamp,
            fromStation = from,
            toStation = to,
            balance = balance,
            rawBlockHex = ByteParser.toHex(block)
        )
    }
}
