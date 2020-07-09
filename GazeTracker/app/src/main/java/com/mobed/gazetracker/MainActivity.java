package com.mobed.gazetracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;

//MOBED
import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.callback.InitializationCallback;
import camp.visual.gazetracker.constant.InitializationErrorType;
import camp.visual.gazetracker.device.GazeDevice;

import static java.lang.Float.NaN;

class MyView extends View {
    public MyView(Context context) {
        super(context); // 부모의 인자값이 있는 생성자를 호출한다
    }
    private int x=300;
    private int y=300;
    private int radius = 50;
    private String TAG = "MOBED";
    @Override
    protected void onDraw(Canvas canvas) { // 화면을 그려주는 작업
        Log.d(TAG,canvas.getWidth()+" "+canvas.getHeight());
        Paint paint = new Paint(); // 화면에 그려줄 도구를 셋팅하는 객체
        paint.setColor(Color.RED); // 색상을 지정

        setBackgroundColor(Color.BLACK); // 배경색을 지정
        canvas.drawCircle(x, y, radius, paint); // 원의중심 x,y, 반지름,paint
    }

    public void set_X(int x) {
        this.x = x;
    }

    public void set_Y(int y) {
        this.y = y;
    }
}

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBED";
    private int trackingFPS = 10;
    private GazeTracker gazeTracker;
    private static final String[] PERMISSIONS = new String[]
            {Manifest.permission.CAMERA};
    private static final int REQ_PERMISSION = 1000;
    private MyView m;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        m = new MyView(MainActivity.this);
        setContentView(m);

        checkPermission();
    }

    public boolean dir_exists(String dir_path) {
        boolean ret = false;
        File dir = new File(dir_path);
        if (dir.exists() && dir.isDirectory())
            ret = true;
        return ret;
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check permission status
            if (!hasPermissions(PERMISSIONS)) {

                requestPermissions(PERMISSIONS, REQ_PERMISSION);
            } else {
                checkPermission(true);
            }
        } else {
            checkPermission(true);
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private boolean hasPermissions(String[] permissions) {
        int result;
        // Check permission status in string array
        for (String perms : permissions) {
            if (perms.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!Settings.canDrawOverlays(this)) {
                    return false;
                }
            }
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                // When if unauthorized permission found
                return false;
            }
        }
        // When if all permission allowed
        return true;
    }

    private void checkPermission(boolean isGranted) {
        if (isGranted) {
            permissionGranted();
        } else {
            Log.d(TAG, "not granted permissions");
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraPermissionAccepted) {
                        checkPermission(true);
                    } else {
                        checkPermission(false);
                    }
                }
                break;
        }
    }

    private void permissionGranted() {
        initGaze();
    }

    private void initGaze() {
        GazeDevice gazeDevice = new GazeDevice();
        gazeDevice.addDeviceInfo("SM-G9650", -48f, -3f); // S9+ Screen Resolution: 1440x2960
        String licenseKey = "dev_fdaek1hvcsay7rj24x6c4hwyzb5nf7w3jxpvde8o";
        GazeTracker.initGazeTracker(getApplicationContext(), gazeDevice, licenseKey, initializationCallback);
    }

    private InitializationCallback initializationCallback = new InitializationCallback() {
        @Override
        public void onInitialized(GazeTracker gazeTracker, int error) {
            if (gazeTracker != null) {
                initSuccess(gazeTracker);
            } else {
                String err = "";
                if (error == InitializationErrorType.ERROR_CAMERA_PERMISSION) {
                    // When if camera permission doesn not exists
                    err = "required permission not granted";
                } else if (error == InitializationErrorType.ERROR_AUTHENTICATE) {
                    // Authentication failure (License Key)
                    err = "authentication failed";
                } else {
                    // Gaze library initialization failure
                    // It can ba caused by several reasons(i.e. Out of memory).
                    err = "init gaze library fail";
                }
                Log.w(TAG, "error description: " + err);
            }
        }
    };

    private void initSuccess(GazeTracker gazeTracker) {
        this.gazeTracker = gazeTracker;
        this.gazeTracker.setTrackingFPS(trackingFPS);
        this.gazeTracker.setGazeCallback(gazeCallback);
        this.gazeTracker.startTracking();
    }

    private GazeCallback gazeCallback = new GazeCallback() {
        @Override
        public void onGaze(long timestamp, float x, float y, int state) {
            Log.i(TAG, "gaze coord (" + x + " x " + y + ")");
        }

        @Override
        public void onFilteredGaze(long timestamp, float x, float y, int state) {
            Log.i(TAG, "gaze filterd coord (" + x + " x " + y + ")");
            if(x!=NaN) {
                int setx, sety;
                if (x<0) setx=0;
                else setx = (int) x;
                if (y<0) sety=0;
                else sety = (int) y;
                m.set_X(setx);
                m.set_Y(sety);
                m.invalidate();
            }
        }
    };
}