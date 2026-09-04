package com.mrtcurrencycheck.nfc.parser

/** Low-level byte helpers for decoding FeliCa blocks. */
internal object ByteParser {

    private const val HEX = "0123456789ABCDEF"

    fun toHex(bytes: ByteArray): String = buildString(bytes.size * 3) {
        for (b in bytes) {
            val u = b.toInt() and 0xFF
            append(HEX[u shr 4]); append(HEX[u and 0x0F]); append(' ')
        }
    }.trim()

    fun byteAt(bytes: ByteArray, offset: Int): Int = bytes[offset].toInt() and 0xFF

    /** 3-byte little-endian integer (used for balance). */
    fun int24LE(bytes: ByteArray, offset: Int): Int =
        (byteAt(bytes, offset + 2) shl 16) or
            (byteAt(bytes, offset + 1) shl 8) or
            byteAt(bytes, offset)

    /** 3-byte big-endian integer (used for the packed timestamp). */
    fun int24BE(bytes: ByteArray, offset: Int): Int =
        (byteAt(bytes, offset) shl 16) or
            (byteAt(bytes, offset + 1) shl 8) or
            byteAt(bytes, offset + 2)
}
