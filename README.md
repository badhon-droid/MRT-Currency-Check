# MRT Currency Check

Minimal, offline, single-screen Android app that reads the **balance** of a Dhaka
**MRT Pass** or **Rapid Pass** by tapping the card on the back of the phone.

- Kotlin + Jetpack Compose (Material 3), simple MVVM
- NFC foreground reader mode only — no background tag grabbing
- Fully offline, no backend, no internet permission
- Dark mode + dynamic color, haptic tick on a successful read
- Tap again to refresh

## How it reads the balance

The cards are **Sony FeliCa**, surfaced on Android as `NfcF`. The last ~20
transactions (and therefore the current balance) live on **service code `0x220F`**
and are stored in the clear — a FeliCa *Read Without Encryption* command (`0x06`)
recovers them. **No encryption keys, no DMTCL API.** Each 16-byte block holds a
timestamp, from/to station codes, and a little-endian 24-bit balance; block 0 is
the most recent event, so its balance is the current one.

## Project layout

```
app/src/main/java/com/mrtcurrencycheck/
├── MainActivity.kt              # enableReaderMode + Compose host + haptics
├── MainViewModel.kt             # ScanState, reads off the main thread
├── model/                       # Transaction, CardReadResult, ScanState
├── nfc/
│   ├── CardReader.kt            # pluggable interface (swap implementations here)
│   ├── FelicaCardReader.kt      # real NFC-F reader
│   ├── MockCardReader.kt        # fake data for emulator/UI work
│   ├── NfcCommandGenerator.kt   # builds the Read Without Encryption command
│   └── parser/                  # ByteParser, TransactionParser, Timestamp/Station
└── ui/                          # ScanScreen + theme
```

## Build & run

1. Open the folder in Android Studio (Giraffe or newer). Let it sync — it will
   generate the Gradle wrapper and download dependencies.
2. Run on a **physical Android phone with NFC** (the emulator has no NFC radio).
3. Open the app and tap your MRT Pass / Rapid Pass flat against the back.

**No real card / emulator?** In `MainActivity.kt` change
`private val cardReader: CardReader = FelicaCardReader()` to `MockCardReader()`
and tap anything — it returns sample data so you can exercise the full UI.

## Limitations (real-world)

- **Read-only.** Write access is locked by DMTCL; this app can never change a
  balance, only display it.
- **History depth.** Only the on-card ring buffer (~10–20 entries) is available
  offline; full history needs an official DMTCL solution.
- **Timestamp granularity.** The packed on-card timestamp stores the hour but not
  minutes, so transaction times are hour-accurate.
- **Card variants.** Station-code and block layouts can shift if DMTCL updates
  the card profile; the `nfc/parser` package is the single place to adjust.

## Attribution

The FeliCa command/parse details follow the publicly reverse-engineered format
used by the open-source **MRT Buddy** project (github.com/aniruddha-adhikary/mrt-buddy).
This app is independent and **not affiliated with or endorsed by DMTCL**.
