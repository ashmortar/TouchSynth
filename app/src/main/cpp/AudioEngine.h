//===========================AUDIO ENGINE==================================================
// - setting up an audio stream to default audio device
// - connecting oscillator to audio stream using data callback
// - switching oscillator's wave output on and off
// - closing the stream when its no longer required


#include<aaudio/AAudio.h>
#include "Oscillator.h"

class AudioEngine {
public:
    bool start();
    void stop();
    void restart();
    void setToneOn(bool isToneOn);

private:
    Oscillator oscillator_;
    AAudioStream *stream_;
};


#endif //TOUCHSYNTH2_AUDIOENGINE_H
