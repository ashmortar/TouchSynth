package studios.ashmortar.touchsynth;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{
    @BindView(R.id.touchEnv) TextView touchEnv;
    @BindView(R.id.record_button) TextView recordButton;
    @BindView(R.id.loopPlayback) Switch loopPlayback;
    @BindView(R.id.key0) TextView viewKey0;
    @BindView(R.id.key1) TextView viewKey1;
    @BindView(R.id.key2) TextView viewKey2;
    @BindView(R.id.key3) TextView viewKey3;
    @BindView(R.id.key4) TextView viewKey4;
    @BindView(R.id.key5) TextView viewKey5;
    @BindView(R.id.key6) TextView viewKey6;
    @BindView(R.id.key7) TextView viewKey7;
    @BindView(R.id.key8) TextView viewKey8;
    @BindView(R.id.key9) TextView viewKey9;
    @BindView(R.id.key10) TextView viewKey10;
    @BindView(R.id.key11) TextView viewKey11;
    @BindView(R.id.key12) TextView viewKey12;
    @BindView(R.id.key13) TextView viewKey13;
    @BindView(R.id.key14) TextView viewKey14;
    @BindView(R.id.key15) TextView viewKey15;
    @BindView(R.id.key16) TextView viewKey16;
    @BindView(R.id.key17) TextView viewKey17;
    @BindView(R.id.key18) TextView viewKey18;
    @BindView(R.id.key19) TextView viewKey19;
    @BindView(R.id.key20) TextView viewKey20;
    @BindView(R.id.key21) TextView viewKey21;

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
    private int offSet = 160;
    private float keyPressedAlpha = 1f;
    private float keyReleasedAlpha = 0.5f;

    //native methods
    public native void startOscillator();
    public native void stopOscillator();
    public native void startEngine();
    public native void stopEngine();
    public native void setRecording(boolean isRecording);
    public native void setPlaying(boolean isPlaying);
    private native void setLooping(boolean isOn);
    private native void touchEvent(int action, double freq, double amp);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidthInPx = size.x;
        displayHeightinPx = size.y;
        keyWidthInPx = 129;//displayWidthInPx / numberOfKeys;
//        setKeyViewWidth(keyWidthInPx);
        super.onCreate(savedInstanceState);
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
        float x = event.getRawX();
        float y = event.getRawY();

        //change alphas if touch event is within bounds of a key
        if (viewKey0.getLeft() + offSet <= x && x <= viewKey0.getRight() + offSet && viewKey0.getTop() <= y) {
            viewKey0.setAlpha(keyPressedAlpha);
        } else {
            viewKey0.setAlpha(keyReleasedAlpha);
        }
        if (viewKey1.getLeft() + offSet <= x && x <= viewKey1.getRight() + offSet && viewKey1.getTop() <= y) {
            viewKey1.setAlpha(keyPressedAlpha);
        } else {
            viewKey1.setAlpha(keyReleasedAlpha);
        }
        if (viewKey2.getLeft() + offSet <= x && x <= viewKey2.getRight() + offSet && viewKey2.getTop() <= y) {
            viewKey2.setAlpha(keyPressedAlpha);
        } else {
            viewKey2.setAlpha(keyReleasedAlpha);
        }
        if (viewKey3.getLeft() + offSet <= x && x <= viewKey3.getRight() + offSet && viewKey3.getTop() <= y) {
            viewKey3.setAlpha(keyPressedAlpha);
        } else {
            viewKey3.setAlpha(keyReleasedAlpha);
        }
        if (viewKey4.getLeft() + offSet <= x && x <= viewKey4.getRight() + offSet && viewKey4.getTop() <= y) {
            viewKey4.setAlpha(keyPressedAlpha);
        } else {
            viewKey4.setAlpha(keyReleasedAlpha);
        }
        if (viewKey5.getLeft() + offSet <= x && x <= viewKey5.getRight() + offSet && viewKey5.getTop() <= y) {
            viewKey5.setAlpha(keyPressedAlpha);
        } else {
            viewKey5.setAlpha(keyReleasedAlpha);
        }
        if (viewKey6.getLeft() + offSet <= x && x <= viewKey6.getRight() + offSet && viewKey6.getTop() <= y) {
            viewKey6.setAlpha(keyPressedAlpha);
        } else {
            viewKey6.setAlpha(keyReleasedAlpha);
        }
        if (viewKey7.getLeft() + offSet <= x && x <= viewKey7.getRight() + offSet && viewKey7.getTop() <= y) {
            viewKey7.setAlpha(keyPressedAlpha);
        } else {
            viewKey7.setAlpha(keyReleasedAlpha);
        }
        if (viewKey8.getLeft() + offSet <= x && x <= viewKey8.getRight() + offSet && viewKey8.getTop() <= y) {
            viewKey8.setAlpha(keyPressedAlpha);
        } else {
            viewKey8.setAlpha(keyReleasedAlpha);
        }
        if (viewKey9.getLeft() + offSet <= x && x <= viewKey9.getRight() + offSet && viewKey9.getTop() <= y) {
            viewKey9.setAlpha(keyPressedAlpha);
        } else {
            viewKey9.setAlpha(keyReleasedAlpha);
        }
        if (viewKey10.getLeft() + offSet <= x && x <= viewKey10.getRight() + offSet && viewKey10.getTop() <= y) {
            viewKey10.setAlpha(keyPressedAlpha);
        } else {
            viewKey10.setAlpha(keyReleasedAlpha);
        }
        if (viewKey11.getLeft() + offSet <= x && x <= viewKey11.getRight() + offSet && viewKey11.getTop() <= y) {
            viewKey11.setAlpha(keyPressedAlpha);
        } else {
            viewKey11.setAlpha(keyReleasedAlpha);
        }
        if (viewKey12.getLeft() + offSet <= x && x <= viewKey12.getRight() + offSet && viewKey12.getTop() <= y) {
            viewKey12.setAlpha(keyPressedAlpha);
        } else {
            viewKey12.setAlpha(keyReleasedAlpha);
        }
        if (viewKey13.getLeft() + offSet <= x && x <= viewKey13.getRight() + offSet && viewKey13.getTop() <= y) {
            viewKey13.setAlpha(keyPressedAlpha);
        } else {
            viewKey13.setAlpha(keyReleasedAlpha);
        }
        if (viewKey14.getLeft() + offSet <= x && x <= viewKey14.getRight() + offSet && viewKey14.getTop() <= y) {
            viewKey14.setAlpha(keyPressedAlpha);
        } else {
            viewKey14.setAlpha(keyReleasedAlpha);
        }
        if (viewKey15.getLeft() + offSet <= x && x <= viewKey15.getRight() + offSet && viewKey15.getTop() <= y) {
            viewKey15.setAlpha(keyPressedAlpha);
        } else {
            viewKey15.setAlpha(keyReleasedAlpha);
        }
        if (viewKey16.getLeft() + offSet <= x && x <= viewKey16.getRight() + offSet && viewKey16.getTop() <= y) {
            viewKey16.setAlpha(keyPressedAlpha);
        } else {
            viewKey16.setAlpha(keyReleasedAlpha);
        }
        if (viewKey17.getLeft() + offSet <= x && x <= viewKey17.getRight() + offSet && viewKey17.getTop() <= y) {
            viewKey17.setAlpha(keyPressedAlpha);
        } else {
            viewKey17.setAlpha(keyReleasedAlpha);
        }
        if (viewKey18.getLeft() + offSet <= x && x <= viewKey18.getRight() + offSet && viewKey18.getTop() <= y) {
            viewKey18.setAlpha(keyPressedAlpha);
        } else {
            viewKey18.setAlpha(keyReleasedAlpha);
        }
        if (viewKey19.getLeft() + offSet <= x && x <= viewKey19.getRight() + offSet && viewKey19.getTop() <= y) {
            viewKey19.setAlpha(keyPressedAlpha);
        } else {
            viewKey19.setAlpha(keyReleasedAlpha);
        }
        if (viewKey20.getLeft() + offSet <= x && x <= viewKey20.getRight() + offSet && viewKey20.getTop() <= y) {
            viewKey20.setAlpha(keyPressedAlpha);
        } else {
            viewKey20.setAlpha(keyReleasedAlpha);
        }
        if (viewKey21.getLeft() + offSet <= x && x <= viewKey21.getRight() + offSet && viewKey21.getTop() <= y) {
            viewKey21.setAlpha(keyPressedAlpha);
        } else {
            viewKey21.setAlpha(keyReleasedAlpha);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            viewKey0.setAlpha(keyReleasedAlpha);
            viewKey1.setAlpha(keyReleasedAlpha);
            viewKey2.setAlpha(keyReleasedAlpha);
            viewKey3.setAlpha(keyReleasedAlpha);
            viewKey4.setAlpha(keyReleasedAlpha);
            viewKey5.setAlpha(keyReleasedAlpha);
            viewKey6.setAlpha(keyReleasedAlpha);
            viewKey7.setAlpha(keyReleasedAlpha);
            viewKey8.setAlpha(keyReleasedAlpha);
            viewKey9.setAlpha(keyReleasedAlpha);
            viewKey10.setAlpha(keyReleasedAlpha);
            viewKey11.setAlpha(keyReleasedAlpha);
            viewKey12.setAlpha(keyReleasedAlpha);
            viewKey13.setAlpha(keyReleasedAlpha);
            viewKey14.setAlpha(keyReleasedAlpha);
            viewKey15.setAlpha(keyReleasedAlpha);
            viewKey16.setAlpha(keyReleasedAlpha);
            viewKey17.setAlpha(keyReleasedAlpha);
            viewKey18.setAlpha(keyReleasedAlpha);
            viewKey19.setAlpha(keyReleasedAlpha);
            viewKey20.setAlpha(keyReleasedAlpha);
            viewKey21.setAlpha(keyReleasedAlpha);
        }

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
            double freq = normalizeX(event.getX(), Constants.RANGE_E2, Constants.SCALE_MINOR,false);
            double amp = normalizeY(event.getY());
            touchEvent(event.getAction(), freq, amp);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        stopOscillator();
        super.onDestroy();
    }

    public double normalizeX(float xVal, double RANGE, int[] SCALE, boolean isContinuous) {
        double freq;
        double min = displayWidthInPx + -1;
        double max = 270.00;
        double a = RANGE;
        double b = RANGE * 8;
        double twelfthRootTwo = 1.059463094359;//1.059463094359; //Math.pow(2, (1/12));
        int semitone;
        int keyPressed;
        if (isContinuous) {
            freq = ((b - a)*((xVal) - min))/(max - min) + a;
            Log.d(TAG, "normalizeX: xVal = " + xVal);
            Log.d(TAG, "normalizeX: freq = " + valueOf(freq));
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
            if (xVal < 0) {
                xVal = 0;
            }
            keyPressed = (int) (((xVal)/keyWidthInPx)*-1);
            keyPressed = keyPressed + 21;
            if (0 <= keyPressed && keyPressed <= 21){
                semitone = SCALE[keyPressed];
            } else {
                semitone = 0;
            }
            freq = 2 * RANGE * (Math.pow(twelfthRootTwo, semitone));
            if (freqCheck != freq) {
                vibe.vibrate(VibrationEffect.createOneShot(vibeLength, vibeAmplitude));
                freqCheck = freq;
            }
//            Log.d(TAG, "normalizeX: xVal/keyWidth = "+ xVal/keyWidthInPx);
//            Log.d(TAG, "normalizeX: keyPressed = " + keyPressed);
//            Log.d(TAG, "normalizeX:  semiTone = " + semitone);
//            Log.d(TAG, "normalizeX: RANGE = " + RANGE);
//            Log.d(TAG, "normalizeX: multiple(twelfthRootTwo^semiTone = " + (Math.pow(twelfthRootTwo, semitone)));
//            Log.d(TAG, "normalizeX: freq = " + freq);
//            Log.d(TAG, "normalizeX: xPos = " + xVal);
            return freq;
        }
    }

    public double normalizeY(float yVal) {
        if (yVal < 0) {
            return 0.6;
        } else {
            double amplitude = ((yVal*-1 + 900)/900) * 0.6;
            Log.d(TAG, "normalizeY: yVal = " + yVal);
            Log.d(TAG, "normalizeY: amplitude = " + amplitude);
            return amplitude;
        }
    }

    public void setKeyViewWidth(int keyWidthInPx) {
        Resources r= getResources();
        String name = getPackageName();
        for (int i = 0; i <22; i++){
            String viewIdString = "R.id.key" + Integer.toString(i);
            int viewId = r.getIdentifier(viewIdString, "id", name);
            View view_instance = (View) findViewById(viewId);
            ViewGroup.LayoutParams params = view_instance.getLayoutParams();
            params.width = keyWidthInPx;
            view_instance.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
