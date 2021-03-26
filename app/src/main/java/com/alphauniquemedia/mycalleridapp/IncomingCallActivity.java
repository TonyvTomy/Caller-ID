package com.alphauniquemedia.mycalleridapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alphauniquemedia.mycalleridapp.databinding.ActivityIncomingCallBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomingCallActivity extends AppCompatActivity {

    ActivityIncomingCallBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setWindowParams();

        binding = ActivityIncomingCallBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();


        int callState = getIntent().hasExtra("callState") ? getIntent().getIntExtra("callState", -1) : -1;
        String phNumber = getIntent().hasExtra("incomingNumber") ? getIntent().getStringExtra("incomingNumber") : "";

        Log.d("Ringing", "IncomingCallActivity, callState :" + callState);
        Log.d("Ringing", "IncomingCallActivity, incomingNumber :" + phNumber);


        binding.tvName.setText(R.string.searching);
        binding.tvClose.setVisibility(View.INVISIBLE);


        settingValuesToUI(callState, phNumber);

        setContentView(view);


        binding.tvClose.setOnClickListener(v -> finish());

        binding.getRoot().setOnLongClickListener(v -> {
            //IF YOU WANT (DO YOUR TASK HERE)
            return true;
        });

    }


    public void setWindowParams() {
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.dimAmount = 0;
        wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getWindow().setAttributes(wlp);
    }


    private void settingValuesToUI(int callState, String phNumber) {
        boolean isPhoneNoValid = phNumber == null || phNumber.equals("") || phNumber.length() != 10;

        if (isPhoneNoValid) {
            finish();
            return;
        }

        binding.tvMsg.setText("Incoming call from +91 " + phNumber);

        if (callState == TelephonyManager.CALL_STATE_RINGING) {

            yourMethod(this, phNumber);

        } else if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {

            yourMethod(this, phNumber);

        } else if (callState == TelephonyManager.CALL_STATE_IDLE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(false);
            }

            yourMethod(this, phNumber);

        } else {
            finish();
        }

    }


    //One of the latest implementation of AsyncTask
    private void yourMethod(final Context mContext, final String phNumber) {

        ExecutorService executors = Executors.newSingleThreadExecutor();
        executors.execute(() -> {
            //// do background heavy task here
            String name = getContactName(IncomingCallActivity.this, phNumber) + "";

            new Handler(Looper.getMainLooper()).post(() -> {
                //// Ui thread work like
                if (isContactExists(mContext, phNumber)) {
                    binding.tvStamp.setText(splitUsernameFromData(name));
                    binding.tvName.setText(name);
                } else {
                    binding.tvStamp.setText("n/a");
                    binding.tvName.setText("New Call");
                }
                binding.tvClose.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        });

    }


    //You can split your username
    private String splitUsernameFromData(String username) {

        if (username.equals("") || username.equals(" ")) {
            return "n/a";
        } else {
            String name = username.replaceFirst("^\\s*(?:M(?:iss|rs?|s)|Dr|Rev)\\b[\\s.]*", "");
            if (name.equals("") || name.equals(" ")) {
                return "n/a";
            }
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
    }


    //To check if contact exits
    public boolean isContactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        try (Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null)) {
            assert cur != null;
            if (cur.moveToFirst()) {
                return true;
            }
        }
        return false;
    }


    //To get the contact name
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
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
