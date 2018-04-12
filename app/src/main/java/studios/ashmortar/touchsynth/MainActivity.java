package studios.ashmortar.touchsynth;

import android.Manifest;
import android.content.Context;
import android.graphics.Point;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.autofill.FillRequest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    @BindView(R.id.touchEnv) TextView touchEnv;
    @BindView(R.id.record_button) TextView recordButton;
    @BindView(R.id.loopPlayback) Switch loopPlayback;






    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //declarations
    private int vibeLength = 5;
    private int vibeAmplitude = VibrationEffect.DEFAULT_AMPLITUDE;
    private Vibrator vibe;
    private double freqCheck;
    private static final int TOUCHSYNTH_REQUEST = 0;
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int numberOfKeys = 22;
    private int displayWidthInPx;
    private int displayHeightinPx;
    private int keyWidthInPx;

    //native methods
    public native void startOscillator();
    public native void stopOscillator();
    public native void startEngine();
    public native void stopEngine();
    public native void setRecording(boolean isRecording);
    public native void setPlaying(boolean isPlaying);
    private native void setLooping(boolean isOn);
    private native void touchEvent(int action, double freq);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidthInPx = size.x;
        displayHeightinPx = size.y;
        keyWidthInPx = displayHeightinPx / numberOfKeys;
        Log.d(TAG, "onCreate: dimensions width = " + Integer.toString(displayWidthInPx));
        Log.d(TAG, "onCreate: dimensions height = " + Integer.toString(displayHeightinPx));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startOscillator();
        touchEnv.setOnTouchListener(this);
        recordButton.setOnTouchListener(this);
        loopPlayback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPlaying(isChecked);
                setLooping(isChecked);
            }
        });
    }

    @Override
    public void onResume(){
        //check if we have recording permission
        if (isRecordPermissionGranted()) {
            startEngine();
        } else {
            Log.d(TAG, "requesting record permission");
            requestRecordPermission();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        stopEngine();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        //check that our permission was granted
        if (permissions.length > 0 && permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PERMISSION_GRANTED) {
            startEngine();
        }
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                TOUCHSYNTH_REQUEST);
    }

    private boolean isRecordPermissionGranted() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == recordButton) {
            switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setRecording(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        setRecording(false);
                        break;
                }
        }
        if (v == touchEnv) {
            double freq = normalizeY(event.getY(), Constants.RANGE_E2, Constants.SCALE_MINOR,false);
            touchEvent(event.getAction(), freq);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        stopOscillator();
        super.onDestroy();
    }

    public double normalizeY(float yVal, double RANGE, int[] SCALE, boolean isContinuous) {
        double freq;
        double min = displayHeightinPx + -1;
        double max = 270.00;
        double a = RANGE;
        double b = RANGE * 8;
        double twelfthRootTwo = 1.059463094359;
        int semitone = 0;
        int keyPressed;
        if (isContinuous) {
            freq = ((b - a)*((yVal) - min))/(max - min) + a;
            Log.d(TAG, "normalizeY: yVal = " + valueOf(yVal));
            Log.d(TAG, "normalizeY: freq = " + valueOf(freq));
            return freq;
            // -270.00  through 2670.00 is the yVals (* -1 for low being down)
            //a3 = 220.00 through 880.00
            // min = -270
            // max = 2670
            // a = 220
            // b = 880
            //        (b-a)(x -min)
            // f(x) = -------------- + a
            //          max - min
        } else {
            //string instruments often have a range of 3-4 octaves (cello = c2 - c6)
            // so maybe 2-5 in each scale? no less than 2-4 or 3-5
            //
            //that would mean we'd need at least 22 cases for scale snapped pitch
            // this can be set dynamically using displayHeightInPx / 22 (or 29 for
            //an additional octave) to get the increment value
            // increment value is locked on create into keyWidthInPx;
            //
            //fn=fo*a^n
            //where
            //fo = defined note = RANGE
            //n = number of semitones from defined note (fo) = keyPressed
            //a = (2)^(1/12) = Math.pow(2, (1/12))
            //Math.floor(yVal/keyWidthInPx) = "keyPressed";
            //so keyboard "key pressed" will depend on scale used which must define
            // both the lowest note of the scale but also the semitone
            //interval numbers for each "key" so that frequency can be generated via math;
            //real example A major would need to give both fo=220 but also
            //[0 , 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24, 26, 28, 29, 31, 33, 35]
            keyPressed = (int) (yVal/keyWidthInPx);
            if (keyPressed >= 0){
                semitone = SCALE[keyPressed];
            } else {
                semitone = 22;
            }
            freq = 2 * RANGE * (Math.pow(twelfthRootTwo, semitone));
//            if (freqCheck != freq) {
//                vibe.vibrate(VibrationEffect.createOneShot(vibeLength, vibeAmplitude));
//                freqCheck = freq;
//            }
            Log.d(TAG, "normalizeY: keyPressed = " + keyPressed);
            Log.d(TAG, "normalizeY:  semiTone = " + semitone);
            Log.d(TAG, "normalizeY: RANGE = " + RANGE);
            Log.d(TAG, "normalizeY: multiple(twelfthRootTwo^semiTone = " + (Math.pow(twelfthRootTwo, semitone)));
            Log.d(TAG, "normalizeY: freq = " + freq);
            return freq;
        }
    }
}
