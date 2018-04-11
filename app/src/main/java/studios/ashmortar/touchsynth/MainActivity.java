package studios.ashmortar.touchsynth;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final int TOUCHSYNTH_REQUEST = 0;
    public static final String TAG = MainActivity.class.getSimpleName();

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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
            double freq = normalizeY(event.getY());
            touchEvent(event.getAction(), freq);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        stopOscillator();
        super.onDestroy();
    }

    public double normalizeY(float yVal) {
        double frequency;
        double min = -2670.00;
        double max = 270.00;
        double a = 20.00;
        double b = 880.00;

        frequency = ((b - a)*((yVal * -1) - min))/(max - min) + a;
        // -270.00  through 2670.00 is the yVals
        //a3 = 220.00 through 880.00
        // min = -270
        // max = 2670
        // a = 220
        // b = 880
        //        (b-a)(x -min)
        // f(x) = -------------- + a
        //          max - min
        Log.d(TAG, "normalizeY: yVal = " + valueOf(yVal));
        Log.d(TAG, "normalizeY: freq = " + valueOf(frequency));
        return frequency;
    }
}
