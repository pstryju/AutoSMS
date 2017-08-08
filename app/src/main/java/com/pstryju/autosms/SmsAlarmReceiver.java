package com.pstryju.autosms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class SmsAlarmReceiver extends BroadcastReceiver {
    private static String TAG = SmsAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //receive user values from intent
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String messageText = intent.getStringExtra("messageText");
        SharedPreferences sharedPrefs = context.getSharedPreferences("AutoSms", Context.MODE_PRIVATE);
        //check if we need to reset counter after restarting service
        SharedPreferences.Editor spEditor = sharedPrefs.edit();
        boolean resetCounter = sharedPrefs.getBoolean("resetCounter", true);
        //send sms
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);


        int sentCount;
        if(resetCounter) {   //reset counter if first message
            sentCount = 1;
        }
        else { //count if not first message
            sentCount = sharedPrefs.getInt("sentCount", 0);
            sentCount++;
        }

        //save counter in prefs and disable reset counter flag
        spEditor.putInt("sentCount", sentCount);
        spEditor.putBoolean("resetCounter", false);
        spEditor.apply();
        Log.d(TAG, "Messages sent count: " + sentCount);
    }


}
