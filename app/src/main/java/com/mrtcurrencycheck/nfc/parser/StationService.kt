package com.mrtcurrencycheck.nfc.parser

/** Maps the 1-byte station codes stored on the card to readable names (MRT Line 6 + Hatirjheel). */
internal object StationService {

    private val stations = mapOf(
        10 to "Motijheel",
        20 to "Bangladesh Secretariat",
        25 to "Dhaka University",
        30 to "Shahbagh",
        35 to "Karwan Bazar",
        40 to "Farmgate",
        45 to "Bijoy Sarani",
        50 to "Agargaon",
        55 to "Shewrapara",
        60 to "Kazipara",
        65 to "Mirpur 10",
        70 to "Mirpur 11",
        75 to "Pallabi",
        80 to "Uttara South",
        85 to "Uttara Center",
        90 to "Uttara North",
        // Hatirjheel feeder stops
        13 to "Mohanagar",
        16 to "Rampura",
        17 to "Badda",
        19 to "Police Plaza",
        28 to "FDC"
    )

    fun name(code: Int): String = stations[code] ?: "Unknown ($code)"
}
