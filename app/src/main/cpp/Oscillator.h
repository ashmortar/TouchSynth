#include <atomic>
#include <stdint.h>

class Oscillator {
public:
    void setWaveOn(bool isWaveOn);
    void setSampleRate(int32_t sampleRate);
    void setFrequency(double frequency);
    void render(float *audioData, int32_t numFrames);
    void setAmplitude(double amplitude);

private:
    std::atomic<bool> isWaveOn_{false};
    double phase_= 0.0;
    double phaseIncrement_ = 0.0;
    double frequency_ = 440.0;
    double amplitude_ = 0.5;

};


