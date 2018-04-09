package studios.ashmortar.touchsynth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private native void touchEvent(int action);
    private native void startEngine();
    private native void stopEngine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEngine();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchEvent(event.getAction());
        return super.onTouchEvent(event);
    }

    @Override
    public void onDestroy() {
        stopEngine();
        super.onDestroy();
    }
}
