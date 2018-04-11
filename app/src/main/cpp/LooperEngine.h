//===========================AUDIO ENGINE==================================================
// - setting up an audio stream to default audio device
// - connecting oscillator to audio stream using data callback
// - switching oscillator's wave output on and off
// - closing the stream when its no longer required


#include<aaudio/AAudio.h>
#include <cstdint>
#include <atomic>
#include <memory>
#include "SoundRecording.h"

class AudioEngine {

public:
    void start();
    void stop();
    void restart();
    aaudio_data_callback_result_t recordingCallback(float *audioData, int32_t numFrames);
    aaudio_data_callback_result_t playbackCallback(float *audioData, int32_t numFrames);
    void setRecording(bool isRecording);
    void setPlaying(bool isPlaying);
    void setLooping(bool isOn);

private:
    std::atomic<bool> mIsRecording = {false};
    std::atomic<bool> mIsPlaying = {false};
    AAudioStream* mPlaybackStream = nullptr;
    AAudioStream* mRecordingStream = nullptr;
    SoundRecording mSoundRecording;
    void stopStream(AAudioStream *stream) const;
    void closeStream(AAudioStream **stream) const;
};


