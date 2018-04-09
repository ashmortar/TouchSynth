package studios.ashmortar.touchsynth;

import android.Manifest;
import android.content.pm.PackageManager;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //declarations
    private static final int TOUCHSYNTH_REQUEST = 0;
    public static final String TAG = MainActivity.class.getSimpleName();

    public native void startEngine();
    public native void stopEngine();
    public native void setRecording(boolean isRecording);
    public native void setPlaying(boolean isPlaying);
    private native void setLooping(boolean isOn);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View recordButton = findViewById(R.id.button_record);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setRecording(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        setRecording(false);
                        break;
                }
                return true;
            }
        });

        View playButton = findViewById(R.id.button_play);
        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        setPlaying(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        setPlaying(false);
                        break;
                }
                return true;
            }
        });

        Switch loopButton = findViewById(R.id.switch_loop);
        loopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }



    //==============below is ui from first codelab for a440 oscillator on touch===================
//    private native void touchEvent(int action);
//    private native void startEngine();
//    private native void stopEngine();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        startEngine();
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        touchEvent(event.getAction());
//        return super.onTouchEvent(event);
//    }
//
//    @Override
//    public void onDestroy() {
//        stopEngine();
//        super.onDestroy();
//    }
}
