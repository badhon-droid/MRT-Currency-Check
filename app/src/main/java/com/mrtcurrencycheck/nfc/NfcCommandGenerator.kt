package com.mrtcurrencycheck.nfc

/**
 * Builds a FeliCa "Read Without Encryption" command (command code 0x06).
 *
 * The Dhaka MRT Pass / Rapid Pass exposes its transaction history on service
 * code 0x220F in plaintext — no mutual authentication or keys required — so a
 * plain Read Without Encryption is enough to recover the balance.
 *
 * Each block is 16 bytes. We read in chunks of 10 blocks (the per-command max),
 * so two calls (start 0 and start 10) cover the ~20-entry history ring.
 */
internal object NfcCommandGenerator {

    private const val CMD_READ_WO_ENCRYPTION = 0x06
    private const val SERVICE_CODE = 0x220F
    private const val BLOCK_CONTROL_BYTE = 0x80

    fun readCommand(
        idm: ByteArray,
        startBlock: Int,
        blockCount: Int = 10
    ): ByteArray {
        val serviceLo = (SERVICE_CODE and 0xFF).toByte()
        val serviceHi = ((SERVICE_CODE shr 8) and 0xFF).toByte()

        // Two bytes per block-list element: control byte + block number.
        val blockList = ByteArray(blockCount * 2)
        for (i in 0 until blockCount) {
            blockList[i * 2] = BLOCK_CONTROL_BYTE.toByte()
            blockList[i * 2 + 1] = (startBlock + i).toByte()
        }

        val length = 14 + blockList.size
        val cmd = ByteArray(length)
        var i = 0
        cmd[i++] = length.toByte()                  // total length (incl. this byte)
        cmd[i++] = CMD_READ_WO_ENCRYPTION.toByte()  // command code
        idm.copyInto(cmd, i); i += idm.size         // 8-byte card IDm
        cmd[i++] = 0x01                             // number of services
        cmd[i++] = serviceLo
        cmd[i++] = serviceHi
        cmd[i++] = blockCount.toByte()              // number of blocks
        blockList.copyInto(cmd, i)
        return cmd
    }
}
