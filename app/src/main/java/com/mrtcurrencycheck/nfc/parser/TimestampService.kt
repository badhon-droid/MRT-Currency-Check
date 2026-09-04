package com.mrtcurrencycheck.nfc.parser

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Decodes the packed 24-bit timestamp found at bytes 4..6 of each block.
 *
 * Bit layout (within the 24-bit big-endian value):
 *   year  = bits 17..21  (offset from the current century base)
 *   month = bits 13..16
 *   day   = bits  8..12
 *   hour  = bits  3.. 7
 * Minutes are not stored at this granularity, so they default to 0.
 */
internal object TimestampService {

    private val dhaka = ZoneId.of("Asia/Dhaka")

    fun decode(value: Int): LocalDateTime {
        val hour = (value shr 3) and 0x1F
        val day = (value shr 8) and 0x1F
        val month = (value shr 13) and 0x0F
        val yearOffset = (value shr 17) and 0x1F

        val year = baseYear() + yearOffset
        val safeMonth = month.coerceIn(1, 12)
        val safeDay = day.coerceIn(1, 31)
        val safeHour = hour.coerceIn(0, 23)

        return LocalDateTime.of(year, safeMonth, safeDay, safeHour, 0)
    }

    /** Start of the current century, e.g. 2000, so the 5-bit year offset resolves correctly. */
    private fun baseYear(): Int {
        val current = LocalDateTime.now(dhaka).year
        return current - (current % 100)
    }
}
