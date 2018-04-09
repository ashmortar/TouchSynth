
#ifndef TOUCHSYNTH2_SOUNDRECORDING_H
#define TOUCHSYNTH2_SOUNDRECORDING_H

#include <cstdint>
#include <array>
#include <atomic>

#include "Definitions.h"

constexpr int kMAxSamples = 480000; //10s of audio data @48kHz

class SoundRecording {

public:
    int32_t write(const float *sourceData, int32_t numSamples);
    int32_t read(float *targetData, int32_t numSamples);
    bool isFull() const {return (mWriteIndex == kMAxSamples); };
    void setReadPositionToStart() {mReadIndex = 0; };
    void clear() { mWriteIndex = 0; };
    void setLooping(bool isLooping) { mIsLooping = isLooping; };
    int32_t  getLength() const { return  mWriteIndex; };
    static const int32_t getMaxSamples() { return kMAxSamples; };

private:
    std::atomic<int32_t> mWriteIndex { 0 };
    std::atomic<int32_t> mReadIndex { 0 };
    std::atomic<bool> mIsLooping { false };
    std::array<float,kMAxSamples> mData { 0 };
};


#endif //TOUCHSYNTH2_SOUNDRECORDING_H
