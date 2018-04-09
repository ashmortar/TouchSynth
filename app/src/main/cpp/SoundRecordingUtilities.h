//
// Created by ashmo on 4/9/2018.
//

#ifndef TOUCHSYNTH2_SOUNDRECORDINGUTILITIES_H
#define TOUCHSYNTH2_SOUNDRECORDINGUTILITIES_H


#include <cstdint>

float convertInt16ToFloat(int16_t intValue);
void convertArrayInt16toFloat(int16_t *source, float *target, int32_t length0);
void fillArrayWithZeros(float *data, int32_t length);
void convertArrayMonoToStereo(float *data, int32_t numframes);


#endif //TOUCHSYNTH2_SOUNDRECORDINGUTILITIES_H
