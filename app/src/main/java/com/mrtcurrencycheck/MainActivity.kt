package com.mrtcurrencycheck

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrtcurrencycheck.nfc.CardReader
import com.mrtcurrencycheck.nfc.FelicaCardReader
import com.mrtcurrencycheck.ui.ScanScreen
import com.mrtcurrencycheck.ui.theme.MrtTheme

/**
 * The whole app: one Activity, one screen.
 *
 * NFC is read in foreground reader mode only (enabled in onResume, disabled in
 * onPause), so the app never grabs tags while backgrounded. No runtime NFC
 * permission dialog exists on Android — declaring android.permission.NFC in the
 * manifest plus a hardware feature check is all that's required.
 */
class MainActivity : ComponentActivity() {

    // Swap FelicaCardReader() for MockCardReader() to demo on an emulator.
    private val cardReader: CardReader = FelicaCardReader()

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(cardReader) as T
        }
    }

    private var nfcAdapter: NfcAdapter? = null

    private val readerCallback = NfcAdapter.ReaderCallback { tag: Tag ->
        viewModel.onTagDiscovered(tag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        viewModel.onReadSuccess = { vibrateTick() }

        setContent {
            MrtTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                ScanScreen(state = state)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = nfcAdapter
        when {
            adapter == null -> viewModel.setNfcUnavailable("This phone has no NFC hardware.")
            !adapter.isEnabled -> viewModel.setNfcUnavailable("NFC is turned off.")
            else -> {
                viewModel.resetToWaiting()
                enableReader(adapter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    private fun enableReader(adapter: NfcAdapter) {
        // FeliCa cards expose NfcF; skip NDEF checks and platform sounds for speed.
        val flags = NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
        adapter.enableReaderMode(this, readerCallback, flags, null)
    }

    private fun vibrateTick() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }
}
