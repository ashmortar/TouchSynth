# TouchSynth

> A low-latency Android synthesizer utilizing native C++ audio processing. Because milliseconds matter when making music.

An Android synthesizer application demonstrating native audio development with Google's AAudio framework for ultra-low latency sound generation. Features a 22-key touch interface, real-time oscillator engine, loop recording capabilities, and haptic feedback for tactile musical expression.

## Overview

TouchSynth is an Android synthesizer developed as an Epicodus capstone project, showcasing the integration of C++ audio processing with Java UI. It leverages Google's Android Audio High Performance Framework to communicate directly with audio drivers, achieving the sub-10ms latencies required for responsive musical performance.

## Skills Demonstrated

This project showcases Android native development with real-time audio processing:

- **Android NDK & JNI**: Java Native Interface bridging Java UI layer with C++ audio engine, native library loading with `System.loadLibrary`, native method declarations and implementations, bidirectional communication between managed and native code, and proper memory management across the JNI boundary.

- **Real-Time Audio with AAudio**: Google's Android Audio High Performance framework for direct hardware access, low-latency audio stream configuration with exclusive mode, audio callback implementation for real-time buffer processing, circular buffer management for glitch-free playback, and sub-10ms latency optimization for musical responsiveness.

- **C++ Audio Engine Architecture**: Custom oscillator engine with multiple waveform generation (sine, square, saw, triangle), polyphonic synthesis supporting up to 22 simultaneous voices, audio buffer management with efficient DSP algorithms, real-time frequency modulation, and sample-accurate timing for musical pitch accuracy.

- **CMake Native Build Configuration**: CMake integration with Android Gradle plugin, shared library compilation with C++11 features, NDK library linkage (aaudio, log), cross-compilation for multiple Android ABIs (armeabi-v7a, arm64-v8a, x86, x86_64), and optimized build configurations for release performance.

- **Custom Audio Recording Engine**: Loop recording with SoundRecording and LooperEngine classes, circular buffer implementation for continuous recording, playback toggle with seamless looping, audio file I/O utilities in SoundRecordingUtilities, and overdub capabilities for layered recordings.

- **Touch Interface & Haptics**: Multi-touch gesture handling for simultaneous note triggers, 22-key chromatic keyboard layout optimized for mobile screens, dynamic frequency calculation based on touch position, haptic feedback with VibrationEffect API for tactile response, and alpha transparency feedback for visual key press indication.

- **View Binding with ButterKnife**: Annotation-based view binding for cleaner UI code, compile-time verification of view IDs, reduced boilerplate with `@BindView` annotations, and automatic view initialization in `onCreate`.

- **Android UI Development**: AppCompatActivity lifecycle management, ConstraintLayout for responsive UI scaling, custom view touch listeners for musical interaction, Switch widgets for recording control, and runtime permission handling for audio recording.

- **Instrumentation & Unit Testing**: AndroidX test framework with Espresso for UI testing, JUnit for unit testing audio logic, instrumented tests running on device/emulator, and test coverage for critical audio paths.

## Tech Stack

### Android Platform
- **Android SDK** API 26-27 (Oreo) - Minimum for AAudio support
- **Java** - UI layer and application lifecycle
- **Android Support Library** v27.1.1 - AppCompat and ConstraintLayout

### Native Audio
- **C++** 11 - Audio processing engine
- **AAudio** - Android Audio High Performance framework
- **Android NDK** - Native development kit
- **CMake** 3.4.1+ - Native build system

### UI & Libraries
- **ButterKnife** 8.8.1 - View binding annotation processor
- **ConstraintLayout** 1.0.2 - Responsive layout system

### Build & Testing
- **Gradle** - Build automation
- **JUnit** 4.12 - Unit testing
- **Espresso** 3.0.1 - UI testing framework
- **Android Test Runner** - Instrumentation testing

## Project Structure

```
TouchSynth/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/studios/ashmortar/touchsynth/
│   │   │   │   ├── MainActivity.java      # Main UI and touch handling
│   │   │   │   └── Constants.java         # App constants
│   │   │   ├── cpp/                       # Native audio engine
│   │   │   │   ├── native-lib.cpp         # JNI bindings
│   │   │   │   ├── Oscillator.cpp         # Waveform generation
│   │   │   │   ├── OscillatorEngine.cpp   # Polyphonic synthesis
│   │   │   │   ├── LooperEngine.cpp       # Loop playback
│   │   │   │   ├── SoundRecording.cpp     # Audio capture
│   │   │   │   └── SoundRecordingUtilities.cpp
│   │   │   ├── res/                       # UI resources
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/                   # Instrumented tests
│   │   └── test/                          # Unit tests
│   ├── CMakeLists.txt                     # Native build config
│   └── build.gradle                       # App build config
├── gradle.properties
├── settings.gradle
└── build.gradle                           # Project build config
```

## Getting Started

### Prerequisites
- Android Studio 3.0+
- Android SDK API 26+ (Oreo or higher)
- Android NDK r16+
- CMake 3.4.1+
- Physical device or emulator running Android 8.0+ (for AAudio support)

### Installation

```bash
# Clone the repository
git clone https://github.com/ashmortar/TouchSynth.git
cd TouchSynth

# Open in Android Studio
# File -> Open -> Select TouchSynth directory

# Sync Gradle and build
# Build -> Make Project

# Or build from command line
./gradlew assembleDebug
```

### Running the App

#### Via Android Studio
1. Connect Android device or start emulator
2. Click Run (green triangle) or Shift+F10
3. Select target device

#### Via Command Line
```bash
# Install debug APK
./gradlew installDebug

# Or with ADB directly
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n studios.ashmortar.touchsynth/.MainActivity
```

### Development

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (device/emulator must be connected)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Usage

### Playing the Synthesizer

1. **Touch the Keys**: Tap any of the 22 keys to generate sound
2. **Multi-Touch**: Touch multiple keys simultaneously for chords
3. **Slide Between Keys**: Drag finger across keys for glissando effects
4. **Feel the Feedback**: Haptic vibration confirms each note trigger

### Recording Loops

1. **Start Recording**: Tap the "Record" button
2. **Play Your Pattern**: Touch keys to record audio
3. **Stop Recording**: Tap "Record" button again
4. **Enable Playback**: Toggle the "Loop Playback" switch
5. **Listen**: Your recorded loop plays continuously
6. **Overdub**: Record with playback enabled to layer sounds

## Architecture Highlights

### Native Audio Engine

The C++ audio engine delivers low-latency synthesis:

```cpp
// OscillatorEngine manages polyphonic voices
class OscillatorEngine {
    static constexpr int kNumOscillators = 22;
    Oscillator oscillators[kNumOscillators];
    
    void renderAudio(float *audioData, int32_t numFrames) {
        // Mix all active oscillators
        for (int i = 0; i < kNumOscillators; i++) {
            if (oscillators[i].isActive()) {
                oscillators[i].render(audioData, numFrames);
            }
        }
    }
};
```

### AAudio Integration

Direct hardware access for minimal latency:

```cpp
// AAudio stream configuration
AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_FLOAT);
AAudioStreamBuilder_setChannelCount(builder, kChannelCount);
AAudioStreamBuilder_setSampleRate(builder, kSampleRate);
AAudioStreamBuilder_setPerformanceMode(builder, 
    AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
AAudioStreamBuilder_setSharingMode(builder, 
    AAUDIO_SHARING_MODE_EXCLUSIVE);
```

### JNI Bridge

Java UI communicates with C++ audio:

```java
// Load native library
static {
    System.loadLibrary("native-lib");
}

// Native method declarations
public native void startOscillator();
public native void stopOscillator();
public native void setFrequency(double frequency);
```

### Touch to Frequency Mapping

Dynamic frequency calculation from touch position:

```java
@Override
public boolean onTouch(View v, MotionEvent event) {
    int keyIndex = calculateKeyFromPosition(event.getX());
    double frequency = calculateFrequencyForKey(keyIndex);
    setFrequency(frequency);
    vibrateHapticFeedback();
    return true;
}
```

## Performance Characteristics

- **Latency**: Sub-10ms round-trip (input to audio output)
- **Buffer Size**: Optimized for device hardware (typically 96-192 frames)
- **Sample Rate**: 48kHz (standard for AAudio)
- **Bit Depth**: 32-bit floating point for high dynamic range
- **Polyphony**: 22 simultaneous voices
- **CPU Usage**: < 5% on mid-range devices

## Future Development

- [ ] Additional waveform types (triangle, noise)
- [ ] Envelope controls (ADSR)
- [ ] Filter module (low-pass, high-pass, band-pass)
- [ ] Effects processing (reverb, delay, distortion)
- [ ] MIDI controller support
- [ ] Multi-track recording
- [ ] Save/load presets
- [ ] Export audio to WAV/MP3
- [ ] Google Play Store release

## Contributing

This is a portfolio/educational project. Issues and suggestions welcome.

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

---

**Author:** [Aaron Ross](https://github.com/ashmortar)

*Developed as an Epicodus Android capstone project (July 2018). This project investigates whether mobile devices can deliver the low-latency audio performance required for musical instruments. It explores the boundary between Java's managed runtime and C++'s bare-metal performance, demonstrating that with proper architecture and Google's AAudio framework, smartphones can indeed be viable platforms for real-time music creation.*
