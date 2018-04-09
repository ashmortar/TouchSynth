//===========================AUDIO ENGINE==================================================
// - setting up an audio stream to default audio device
// - connecting oscillator to audio stream using data callback
// - switching oscillator's wave output on and off
// - closing the stream when its no longer required
#include "SoundRecordingUtilities.h"
#include "AudioEngine.h"
#include <thread>
#include <mutex>
#include <android/log.h>
#include <aaudio/AAudio.h>


// Double-buffering offers a good tradeoff between latency and protection against glitches.
constexpr int32_t kBufferSizeInBursts = 2;

//oscillator data callback
aaudio_data_callback_result_t dataCallback(
        AAudioStream *stream,
        void *userData,
        void *audioData,
        int32_t numFrames) {
    ((Oscillator *) (userData))->render(static_cast<float*>(audioData), numFrames);
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

//recording data callback
aaudio_data_callback_result_t recordingDataCallback(
        AAudioStream __unused *stream,
        void *userData,
        void *audioData,
        int32_t numFrames) {
    return ((AudioEngine *) userData)->recordingCallback(
            static_cast<float *>(audioData), numFrames);

}
//playback callback
aaudio_data_callback_result_t  playbackDataCallback(
        AAudioStream __unused *stream,
        void *userData,
        void *audioData,
        int32_t numFrames) {
    return ((AudioEngine *) userData)->playbackCallback(static_cast<float *>(audioData), numFrames);
}


void errorCallback(AAudioStream __unused *stream,
                   void *userData,
                   aaudio_result_t error){
    if (error == AAUDIO_ERROR_DISCONNECTED){
        //The error callback expects to return immediately so it's not safe to restart our streams
        //in here. instead we use a separate thread.
        std::function<void(void)> restartFunction = std::bind(&AudioEngine::restart,
                                                              static_cast<AudioEngine *>(userData));
        new std::thread(restartFunction);
    }
}

// Here we declare a new type: StreamBuilder which is a smart pointer to an AAudioStreamBuilder
// with a custom deleter. The function AudioStreamBuilder_delete will be called when the
// object is deleted. Using a smart pointer allows us to avoid memory management of an
// AAudioStreamBuilder.
using StreamBuilder = std::unique_ptr<AAudioStreamBuilder, decltype(&AAudioStreamBuilder_delete)>;

StreamBuilder makeStreamBuilder() {
    AAudioStreamBuilder *builder = nullptr;
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) {
        __android_log_print(ANDROID_LOG_ERROR, __func__, "failed to create stream builder %s (%d)",
        AAudio_convertResultToText(result), result);
        return StreamBuilder(nullptr, &AAudioStreamBuilder_delete);
    }
    return StreamBuilder(builder, &AAudioStreamBuilder_delete);
}


void AudioEngine::start() {
    StreamBuilder playbackBuilder = makeStreamBuilder();
    AAudioStreamBuilder_setFormat(playbackBuilder.get(), AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setChannelCount(playbackBuilder.get(), kChannelCountStereo);
    AAudioStreamBuilder_setPerformanceMode(playbackBuilder.get(), AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(playbackBuilder.get(), AAUDIO_SHARING_MODE_EXCLUSIVE);
    AAudioStreamBuilder_setDataCallback(playbackBuilder.get(), ::playbackDataCallback, this);
    AAudioStreamBuilder_setErrorCallback(playbackBuilder.get(), ::errorCallback, this);

    aaudio_result_t result = AAudioStreamBuilder_openStream(playbackBuilder.get(), &mPlaybackStream);

    if (result != AAUDIO_OK){
        __android_log_print(ANDROID_LOG_DEBUG, __func__,
                            "Error opening playback stream %s",
                            AAudio_convertResultToText(result));
        return;
    }

    //get the sample rate from playback stream so we can requeue the recording stream
    int32_t sampleRate = AAudioStream_getSampleRate(mPlaybackStream);

    result = AAudioStream_requestStart(mPlaybackStream);
    if (result != AAUDIO_OK){
        __android_log_print(ANDROID_LOG_DEBUG, __func__,
                            "Error starting playback stream %s",
                            AAudio_convertResultToText(result));
        closeStream(&mPlaybackStream);
        return;
    }

    StreamBuilder recordingBuilder = makeStreamBuilder();
    AAudioStreamBuilder_setDirection(recordingBuilder.get(), AAUDIO_DIRECTION_INPUT);
    AAudioStreamBuilder_setPerformanceMode(recordingBuilder.get(), AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(recordingBuilder.get(), AAUDIO_SHARING_MODE_EXCLUSIVE);
    AAudioStreamBuilder_setFormat(recordingBuilder.get(), AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setSampleRate(recordingBuilder.get(), sampleRate);
    AAudioStreamBuilder_setChannelCount(recordingBuilder.get(), kChannelCountMono);
    AAudioStreamBuilder_setDataCallback(recordingBuilder.get(), ::recordingDataCallback, this);
    AAudioStreamBuilder_setErrorCallback(recordingBuilder.get(), ::errorCallback, this);

    result = AAudioStreamBuilder_openStream(recordingBuilder.get(), &mRecordingStream);

    if (result != AAUDIO_OK){
        __android_log_print(ANDROID_LOG_DEBUG, __func__,
                            "Error opening recording stream %s",
                            AAudio_convertResultToText(result));
        closeStream(&mRecordingStream);
        return;
    }

    result = AAudioStream_requestStart(mRecordingStream);
    if (result != AAUDIO_OK){
        __android_log_print(ANDROID_LOG_DEBUG, __func__,
                            "Error starting recording stream %s",
                            AAudio_convertResultToText(result));
        return;
    }



//    //this stream outputs the oscillator
//    AAudioStreamBuilder *streamBuilder;
//    AAudio_createStreamBuilder(&streamBuilder);
//    AAudioStreamBuilder_setFormat(streamBuilder, AAUDIO_FORMAT_PCM_FLOAT);
//    AAudioStreamBuilder_setChannelCount(streamBuilder, 2);
//    AAudioStreamBuilder_setPerformanceMode(streamBuilder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
//    AAudioStreamBuilder_setDataCallback(streamBuilder, ::dataCallback, &oscillator_);
//    AAudioStreamBuilder_setErrorCallback(streamBuilder, ::errorCallback, this);
//
//    //opens the stream
//    aaudio_result_t result = AAudioStreamBuilder_openStream(streamBuilder, &stream_);
//    if (result != AAUDIO_OK) {
//        __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", "error opening stream %s", AAudio_convertResultToText(result));
//        return false;
//    }
//
//    //retrieve the sample rate of the stream for our oscillator
//    int32_t sampleRate = AAudioStream_getSampleRate(stream_);
//    oscillator_.setSampleRate(sampleRate);
//
//    //set the buffer size
//    AAudioStream_setBufferSizeInFrames(
//            stream_, AAudioStream_getFramesPerBurst(stream_) * kBufferSizeInBursts);
//
//    // start the stream
//    result = AAudioStream_requestStart(stream_);
//    if (result != AAUDIO_OK) {
//        __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", "Error starting stream %s", AAudio_convertResultToText(result));
//        return false;
//    }
//    AAudioStreamBuilder_delete(streamBuilder);
//    return true;
}

void AudioEngine::restart() {
    static std::mutex restartingLock;
    if (restartingLock.try_lock()){
        stop();
        start();
        restartingLock.unlock();
    }
}

void AudioEngine::stop() {
    stopStream(mPlaybackStream);
    closeStream(&mPlaybackStream);
    stopStream(mRecordingStream);
    closeStream(&mRecordingStream);
    stopStream(mOscillatorStream);
    closeStream(&mOscillatorStream);
}

void AudioEngine::setToneOn(bool isToneOn) {
    oscillator_.setWaveOn(isToneOn);
}

aaudio_data_callback_result_t AudioEngine::recordingCallback(float *audioData, int32_t numFrames) {
    if (mIsRecording) {
        int32_t framseWritten = mSoundRecording.write(audioData, numFrames);
        if (framseWritten == 0) mIsRecording = false;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

aaudio_data_callback_result_t AudioEngine::playbackCallback(float *audioData, int32_t numFrames) {
    fillArrayWithZeros(audioData, numFrames * kChannelCountStereo);

    if (mIsPlaying) {
        int32_t framesRead = mSoundRecording.read(audioData, numFrames);
        convertArrayMonoToStereo(audioData, framesRead);
        if (framesRead < numFrames) mIsPlaying = false;
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

void AudioEngine::setRecording(bool isRecording) {
    if (isRecording) mSoundRecording.clear();
    mIsRecording = isRecording;
}

void AudioEngine::setPlaying(bool isPlaying) {
    if (isPlaying) mSoundRecording.setReadPositionToStart();
    mIsPlaying = isPlaying;
}

void AudioEngine::stopStream(AAudioStream *stream) const {

    static std::mutex stoppingLock;
    stoppingLock.lock();
    if (stream != nullptr) {
        aaudio_result_t result = AAudioStream_requestStop(stream);
        if (result != AAUDIO_OK) {
            __android_log_print(ANDROID_LOG_DEBUG, __func__, "Error stopping stream %s", AAudio_convertResultToText(result));
        }
    }
    stoppingLock.unlock();
}

void AudioEngine::closeStream(AAudioStream **stream) const {

    static std::mutex closingLock;
    closingLock.lock();
    if (*stream != nullptr){
        aaudio_result_t  result = AAudioStream_close(*stream);
        if (result != AAUDIO_OK) {
            __android_log_print(ANDROID_LOG_DEBUG, __func__, "Error closing stream %s", AAudio_convertResultToText(result));
        }
        *stream = nullptr;
    }
    closingLock.unlock();
}

void AudioEngine::setLooping(bool isOn) {
    mSoundRecording.setLooping(isOn);
}