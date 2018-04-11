#include <jni.h>
#include <android/input.h>
#include <android/log.h>
#include "LooperEngine.h"
#include "OscillatorEngine.h"

static AudioEngine *audioEngine = new AudioEngine();
static OscillatorEngine *oscillatorEngine = new OscillatorEngine();

extern "C" {
JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_touchEvent(JNIEnv *env, jobject obj, jint action, jdouble freq) {
    switch (action) {
        case AMOTION_EVENT_ACTION_DOWN:
//            __android_log_print(ANDROID_LOG_DEBUG, "touch", "%f", freq);
            oscillatorEngine->setToneOn(true, freq);
            break;
        case AMOTION_EVENT_ACTION_UP:
//            __android_log_print(ANDROID_LOG_DEBUG, "touch", "set tone on = false");
            oscillatorEngine->setToneOn(false, freq);
            break;
        case AMOTION_EVENT_ACTION_MOVE:
            oscillatorEngine->setToneOn(true, freq);
            break;
        default:
            break;
    }
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_startEngine(JNIEnv *env, jobject /* this */) {
    audioEngine->start();
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_stopEngine(JNIEnv *env, jobject instance) {
    audioEngine->stop();
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_setRecording(JNIEnv *env, jobject instance,
                                                            jboolean isRecording) {
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib", "Recording? %d", isRecording);
    audioEngine->setRecording(isRecording);
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_setPlaying(JNIEnv *env, jobject instance,
                                                          jboolean isPlaying) {
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib", "Playing? %d", isPlaying);
    audioEngine->setPlaying(isPlaying);
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_setLooping(JNIEnv *env, jobject instance,
                                                          jboolean isOn) {
    audioEngine->setLooping(isOn);
}

JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_startOscillator(JNIEnv *env, jobject instance) {
    oscillatorEngine->start();
}
JNIEXPORT void JNICALL
Java_studios_ashmortar_touchsynth_MainActivity_stopOscillator(JNIEnv *env, jobject instance) {
    oscillatorEngine->stop();
}
}