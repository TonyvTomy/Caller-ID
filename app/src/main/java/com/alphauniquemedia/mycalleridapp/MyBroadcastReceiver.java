package com.alphauniquemedia.mycalleridapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import static android.os.Looper.getMainLooper;

public class MyBroadcastReceiver extends BroadcastReceiver {


    private static MyPhoneStateListener phoneStateListener;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("Ringing", "BroadcastReceiver: onReceive");

        assert intent.getAction() != null;
        if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (phoneStateListener == null) {
                phoneStateListener = MyPhoneStateListener.getInstance(context);
                new MyPhoneStateListener(context);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }

    }

}


class MyPhoneStateListener extends PhoneStateListener {

    Context mContext;
    private static MyPhoneStateListener instance = null;
    String myIncomingNumber = "";

    public MyPhoneStateListener(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static MyPhoneStateListener getInstance(Context context) {
        if (instance == null) {
            instance = new MyPhoneStateListener(context);
        }
        return instance;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        Log.d("Ringing", "BroadcastReceiver: onCallStateChanged No:" + incomingNumber);

        if (state == TelephonyManager.CALL_STATE_RINGING && incomingNumber != null) {
            callMyCallerIdService(state, incomingNumber);
        } else {
            callMyCallerIdService(state, myIncomingNumber);
        }

    }


    private void callMyCallerIdService(int callState, String incomingNumber) {

        if (callState == TelephonyManager.CALL_STATE_RINGING)
            Log.d("Ringing", "BroadcastReceiver: Phone State :- CALL_STATE_RINGING");
        else if (callState == TelephonyManager.CALL_STATE_OFFHOOK)
            Log.d("Ringing", "BroadcastReceiver: Phone State :- CALL_STATE_OFFHOOK");
        else
            Log.d("Ringing", "BroadcastReceiver: Phone State :- CALL_STATE_IDLE");


        if (incomingNumber != null) {

            if (incomingNumber.length() > 10) {
                int startIdx = incomingNumber.length() - 10;
                incomingNumber = incomingNumber.substring(startIdx);
            }

            myIncomingNumber = incomingNumber;

            Log.d("Ringing", "BroadcastReceiver: myIncomingNumber 10dig:" + myIncomingNumber);

            if (callState == TelephonyManager.CALL_STATE_RINGING) {
                Intent serviceIntent = new Intent(mContext, CallerIDForegroundService.class);
                serviceIntent.setAction(Constants.ServiceConstants.ACTION_CALLER_ID);
                serviceIntent.putExtra("callState", callState);
                serviceIntent.putExtra("incomingNumber", myIncomingNumber);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(mContext, serviceIntent);
                } else {
                    mContext.startService(serviceIntent);
                }
            } else if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {

                final Intent intentCallerID = new Intent(mContext, IncomingCallActivity.class);
                intentCallerID.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intentCallerID.putExtras(intentCallerID);
                intentCallerID.putExtra("callState", callState);
                intentCallerID.putExtra("incomingNumber", myIncomingNumber);
                new Handler(getMainLooper()).postDelayed(() -> mContext.startActivity(intentCallerID), 500);
            } else {
                cancelCallerIDService();
                final Intent intentCallerID = new Intent(mContext, IncomingCallActivity.class);
                intentCallerID.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intentCallerID.putExtras(intentCallerID);
                intentCallerID.putExtra("callState", callState);
                intentCallerID.putExtra("incomingNumber", myIncomingNumber);
                new Handler(getMainLooper()).postDelayed(() -> mContext.startActivity(intentCallerID), 500);
                myIncomingNumber = "";
            }

        }

    }

    public void cancelCallerIDService() {
        Intent serviceIntent = new Intent(mContext, CallerIDForegroundService.class);
        mContext.stopService(serviceIntent);
        NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(Constants.ServiceConstants.CALLER_ID_NOTIFICATION_ID);
    }

}
