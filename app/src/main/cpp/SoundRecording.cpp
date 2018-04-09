#include <android/log.h>
#include "SoundRecording.h"

int32_t SoundRecording::write(const float *sourceData, int32_t numSamples) {

    //check that data will fit, if it doesn't just write as much as we can
    if(mWriteIndex + numSamples > kMAxSamples) {
        numSamples = kMAxSamples - mWriteIndex;
    }

    for (int i = 0; i < numSamples; i++) {
        mData[mWriteIndex++] = sourceData[i];
    }
    return numSamples;
}

int32_t SoundRecording::read(float *targetData, int32_t numSamples) {
    int32_t framesRead = 0;
    while (framesRead < numSamples && mReadIndex < mWriteIndex){
        targetData[framesRead++] = mData[mReadIndex++];
        if (mIsLooping && mReadIndex == mWriteIndex) mReadIndex = 0;
    }
    return framesRead;
}