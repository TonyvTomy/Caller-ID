package com.alphauniquemedia.mycalleridapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alphauniquemedia.mycalleridapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_ALL = 101;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnStart.setOnClickListener(v -> {
            if (checkPermissions()) {
                Toast.makeText(this, "You can now Receive incoming call alert", Toast.LENGTH_SHORT).show();
                binding.tvHints.setText("All permissions are allowed you can see your incoming call alert now..");
                startBroadcastReceiver();
            }
        });

        if (isAllPermissionsGranted()) {
            Toast.makeText(this, "You can now Receive incoming call alert", Toast.LENGTH_SHORT).show();
            binding.tvHints.setText("All permissions are allowed you can see your incoming call alert now..");
            startBroadcastReceiver();
        }

    }


    private boolean checkPermissions() {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS
                        , Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE
                        , Manifest.permission.READ_CALL_LOG, Manifest.permission.SYSTEM_ALERT_WINDOW
                        , Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS}, REQUEST_CODE_PERMISSION_ALL);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS
                            , Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.SYSTEM_ALERT_WINDOW
                            , Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS}, REQUEST_CODE_PERMISSION_ALL);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS
                            , Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.SYSTEM_ALERT_WINDOW
                            , Manifest.permission.CALL_PHONE}, REQUEST_CODE_PERMISSION_ALL);
                }
            }

            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_PERMISSION_ALL);
                return false;
            }
        }

        return true;
    }


    //To start Broadcast Receiver
    public void startBroadcastReceiver() {

        if (isAllPermissionsGranted()) {

            IntentFilter filterCalls = new IntentFilter();
            filterCalls.addAction("android.intent.action.PHONE_STATE");
            MyBroadcastReceiver phoneStateReceiver = new MyBroadcastReceiver();
            registerReceiver(phoneStateReceiver, filterCalls);
            Log.d("Ringing", "BroadcastReceiver: All Permissions Allowed");

            Intent serviceIntent = new Intent(this, CallerIDForegroundService.class);
            serviceIntent.setAction(Constants.ServiceConstants.ACTION_CALLER_ID_ALWAYS);

            /*if (!isMyServiceRunning(serviceIntent.getClass())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }*/

        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Ringing", "Running");
                return true;
            }
        }
        Log.i("Ringing", "Not running");
        return false;
    }


    //To check all required permissions granted
    public boolean isAllPermissionsGranted() {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)) {

            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }

        return true;
    }
}