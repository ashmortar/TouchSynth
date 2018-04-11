

#ifndef TOUCHSYNTH2_OSCILLATORENGINE_H
#define TOUCHSYNTH2_OSCILLATORENGINE_H


#include <aaudio/AAudio.h>
#include "Oscillator.h"

class OscillatorEngine {
public:
    bool start();
    void stop();
    void restart();
    void setToneOn(bool isToneOn, double freq);

private:
    Oscillator oscillator_;
    AAudioStream *stream_;

};


#endif //TOUCHSYNTH2_OSCILLATORENGINE_H
