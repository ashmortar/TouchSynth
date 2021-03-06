//basic a440 sine wave oscillator


#include "Oscillator.h"
#include <math.h>
#include <android/log.h>


#define TWO_PI (3.14159 * 2)


void Oscillator::setFrequency(double frequency) {
    frequency_ = frequency;
//    __android_log_print(ANDROID_LOG_DEBUG, "oscillator", "frequency_ = %f", frequency_);
}

void Oscillator::setAmplitude(double amplitude) {
    amplitude_ = amplitude;
}

void Oscillator::setSampleRate(int32_t sampleRate) {
    phaseIncrement_ = (TWO_PI * frequency_) / (double) sampleRate;
}

void Oscillator::setWaveOn(bool isWaveOn) {
    isWaveOn_.store(isWaveOn);
}

void Oscillator::render(float *audioData, int32_t numFramse) {
    if (!isWaveOn_.load()) phase_ = 0;

    for (int i = 0; i < numFramse; i++) {

        if (isWaveOn_.load()) {
            //calculates the next sample value for sin wave
            audioData[i] = (float) (sin(phase_) * amplitude_);

            //increments the phase, handling wrap around
            phase_ += phaseIncrement_;
            if (phase_ > TWO_PI) phase_ -= TWO_PI;

        } else {
            // outputs silence by setting sample value to zero
            // if click when release touch try fade to zero here
//            audioData[i] = 0;
            if (audioData[i-1] > 0) {
//                __android_log_print(ANDROID_LOG_DEBUG, "audioData positive = ", "%f", audioData[i]);
                audioData[i] = audioData[i-1] - 0.001f;
            } else if (audioData[i-1] < 0) {
//                __android_log_print(ANDROID_LOG_DEBUG, "audioData = negative", "%f", audioData[i]);
                audioData[i] = audioData[i-1] + 0.001f;
            } else {
                audioData[i] = 0;
            }
        }
    }
}


