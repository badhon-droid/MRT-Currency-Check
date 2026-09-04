package com.mrtcurrencycheck

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrtcurrencycheck.model.CardReadResult
import com.mrtcurrencycheck.model.ScanState
import com.mrtcurrencycheck.nfc.CardReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns the single screen's [ScanState] and drives card reads through the
 * injected [CardReader]. No Android UI types leak in here, which keeps it
 * unit-testable and lets the reader be swapped freely.
 */
class MainViewModel(private val reader: CardReader) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.WaitingForTap)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    /** Emitted when a read succeeds so the Activity can fire haptic feedback. */
    var onReadSuccess: (() -> Unit)? = null

    fun setNfcUnavailable(reason: String) {
        _state.value = ScanState.NfcUnavailable(reason)
    }

    fun resetToWaiting() {
        if (_state.value is ScanState.NfcUnavailable) return
        _state.value = ScanState.WaitingForTap
    }

    /** Called from the NFC reader-mode callback (a background thread). */
    fun onTagDiscovered(tag: Tag) {
        _state.value = ScanState.Reading
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { reader.read(tag) }
            _state.value = when (result) {
                is CardReadResult.Success -> {
                    onReadSuccess?.invoke()
                    ScanState.Balance(result)
                }
                is CardReadResult.Unsupported ->
                    ScanState.Error("Unsupported card. Tap an MRT Pass or Rapid Pass.")
                is CardReadResult.Error ->
                    ScanState.Error(result.message)
            }
        }
    }
}
