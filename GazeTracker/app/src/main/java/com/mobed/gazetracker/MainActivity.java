package com.mobed.gazetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

//MOBED
import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.callback.InitializationCallback;
import camp.visual.gazetracker.constant.InitializationErrorType;
import camp.visual.gazetracker.device.GazeDevice;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBED";
    private int trackingFPS = 1;
    private GazeTracker gazeTracker;
    private static final String[] PERMISSIONS = new String[]
            {Manifest.permission.CAMERA};
    private static final int REQ_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
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
        }
    };
}