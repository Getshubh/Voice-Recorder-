package com.getshubh.record;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

// Begin, DRS-1500, DRS-1503, 17 July 2020, User to Scan QR Code/record audio using a long press of the power key

public class TransparentPermissionActivity extends Activity {

    private final int mPermissionCode = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window wind = getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        checkPermissions();
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            boolean permissionDeclined = false;
            for (String s : permissions)
                if (checkSelfPermission(s) != PackageManager.PERMISSION_GRANTED)
                    permissionDeclined = true;

            if (permissionDeclined) {
                requestPermissions(permissions, mPermissionCode);
            } else {
                finish();
            }
        } else
            finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mPermissionCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                for (int i = 0, length = permissions.length; i < length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.d("TransparentPermission", "Give Permission");

                    }

            finish();
        }
    }


    //End, DRS-1500, DRS-1503, 17 July 2020, User to Scan QR Code/record audio using a long press of the power key
}



