package com.getshubh.record;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.getshubh.record.adapter.RecordingAdapter;
import com.getshubh.record.ui.dashboard.RecordingListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.getshubh.record.ui.home.RecordingFragment.REQUEST_AUDIO_PERMISSION_CODE;

public class MainActivity extends AppCompatActivity implements RecordingAdapter.OnDeleteClick {

    private static final int REQUEST_CODE = 0;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private Toolbar toolbarTop;
    private TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //  NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        toolbarTop = findViewById(R.id.toolbar_top);
        mTitle = toolbarTop.findViewById(R.id.toolbar_title);

//        initiateDeviceManager();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onItemRefresh() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager().getFragments().get(0);
        if (fragment instanceof RecordingListFragment) {
            ((RecordingListFragment) fragment).fetchRecordings();
            Toast.makeText(this, "Audio File Deleted successfully", Toast.LENGTH_SHORT).show();
        }


    }

    public void setToolbar(String string) {
        mTitle.setText(string);
    }
}