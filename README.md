# Glyph Decibel Meter

An Android app (Kotlin + Jetpack Compose) that captures audio from your phone's microphone, calculates the real-time decibel level, and displays it as a vertical fill bar on screen. It also includes a **Nothing Phone Glyph Matrix integration layer** — fully stubbed so it compiles and runs on any Android phone today, and ready to wire up to the real Nothing Glyph SDK when it's available.

---

## What it does

1. **Captures audio** from the device microphone using `AudioRecord`.
2. **Calculates the dB level** via RMS → dBFS conversion.
3. **Shows a visual meter** — a gradient bar (green→yellow→red) that fills from bottom to top.
4. **Maps dB to Glyph zones** — `GlyphMatrixMapper` converts any dB value into a list of zone states (which rows should light up and at what intensity).
5. **Drives the Glyph controller** — currently a stub that logs to Logcat; swap in the real Nothing SDK implementation whenever it's available.

---

## Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK with API level 34 installed
- A physical device or emulator running Android 8.0+ (API 26+)

### Steps
```bash
# Clone the repo
git clone https://github.com/iama2z/Glyph-Decibel-Meter.git
cd Glyph-Decibel-Meter

# Build a debug APK
./gradlew assembleDebug

# Install directly to a connected device
./gradlew installDebug
```

The app will ask for microphone permission on first launch. Grant it and you'll immediately see the meter react to sound.

---

## Architecture

```
app/src/main/java/com/iama2z/glyphmeter/
├── MainActivity.kt                 Entry point; permission flow; wires everything together
├── audio/
│   ├── AudioCapture.kt             Wraps AudioRecord; emits dB levels as a Kotlin Flow
│   └── DbCalculator.kt             RMS calculation and dBFS conversion
├── glyph/
│   ├── GlyphController.kt          Interface — implement with the real Nothing SDK
│   ├── StubGlyphController.kt      Placeholder — logs zone states to Logcat
│   └── GlyphMatrixMapper.kt        Maps dB → list of GlyphZoneState (which rows, what brightness)
└── ui/
    ├── MeterScreen.kt              Compose UI — vertical bar + dB reading + permission prompt
    └── theme/
        └── Theme.kt                Dark colour scheme (Nothing-style: black, white, red)
```

### Data flow
```
Microphone
    └─ AudioRecord (AudioCapture)
           └─ ShortArray PCM buffer
                  └─ DbCalculator.rmsToDb()
                         └─ Float dBFS value
                                ├─ MeterScreen (Compose UI)          ← updates fill bar
                                └─ GlyphMatrixMapper.map()
                                       └─ List<GlyphZoneState>
                                              └─ GlyphController.update()  ← drives LEDs
```

---

## Plugging in the real Nothing Glyph SDK

When the Nothing Glyph SDK becomes publicly available:

1. **Add the dependency** in `app/build.gradle.kts`:
   ```kotlin
   implementation("com.nothing.ketchum:glyph-sdk:x.y.z")
   ```

2. **Create a real implementation** of `GlyphController`:
   ```kotlin
   class RealNothingGlyphController(context: Context) : GlyphController {
       // Initialise the Nothing SDK session here
       override fun update(zones: List<GlyphZoneState>) {
           // Translate GlyphZoneState → Nothing SDK channel calls
       }
       override fun release() { /* close SDK session */ }
   }
   ```

3. **Replace the stub** in `MainActivity.kt`:
   ```kotlin
   // Before:
   private val glyphController = StubGlyphController()
   // After:
   private val glyphController = RealNothingGlyphController(this)
   ```

The `GlyphMatrixMapper` already produces the right data shape — you only need to translate `GlyphZoneState.intensity` into the SDK's brightness integer/float.

---

## Key dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.02.02 | UI framework |
| AndroidX Core KTX | 1.12.0 | Kotlin extensions |
| AndroidX Lifecycle | 2.7.0 | `lifecycleScope`, coroutines |
| AndroidX Activity Compose | 1.8.2 | `setContent`, permission launcher |

---

## Minimum SDK

**API 26 (Android 8.0 Oreo)** — required for reliable `AudioRecord` behaviour on the target hardware.

---

## License

MIT — do whatever you want with it. Attribution appreciated but not required.