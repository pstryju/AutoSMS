package com.pstryju.autosms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    private Resources mResources;
    private Button toggleServiceButton;
    private EditText phoneNumberEditText;
    private EditText messageTextEditText;
    private TextView smsCountTextView;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    SharedPreferences.OnSharedPreferenceChangeListener spChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences("AutoSms", MODE_PRIVATE);
        final boolean blindMode = mSharedPreferences.getBoolean("blindMode", false);
        if(blindMode)
            setTheme(R.style.blindTheme); //set high-contrast theme when enabled in prefs
        setContentView(R.layout.activity_main);
        toggleServiceButton = (Button) findViewById(R.id.start_service_button);
        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_textedit);
        messageTextEditText = (EditText) findViewById(R.id.message_text_textedit);
        smsCountTextView = (TextView) findViewById(R.id.messages_sent_count_textview);

        //refresh UI when prefs changed
        spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                int smsSent = mSharedPreferences.getInt("sentCount", 0); //refresh sms counter when prefs are changed
                smsCountTextView.setText(Integer.toString(smsSent));
                if(mSharedPreferences.getBoolean("blindMode", false) != blindMode)
                    recreate(); //recreate activity to switch theme after changing prefs
            }
        };
        mSharedPreferences.registerOnSharedPreferenceChangeListener(spChanged);

        //service toggle button
        mResources = getResources();
        toggleServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean serviceRunning = (PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(MainActivity.this, SmsAlarmReceiver.class),
                        PendingIntent.FLAG_NO_CREATE) != null);
                if(serviceRunning) {
                    stopAlarm();
                    toggleServiceButton.setText(mResources.getString(R.string.start_service));
                }
                else {
                    startAlarm();
                    toggleServiceButton.setText(mResources.getString(R.string.stop_service));
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if alarm is registered
        final boolean serviceRunning = (PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.this, SmsAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        Log.d(TAG, "Is service running: " + serviceRunning);
        int messagesSent = mSharedPreferences.getInt("sentCount", 0);
        String phoneNumber = mSharedPreferences.getString("phoneNumber", "");
        String messageText = mSharedPreferences.getString("messageText", "");
        phoneNumberEditText.setText(phoneNumber);
        messageTextEditText.setText(messageText);

        //set proper UI values if alarm registered
        if(serviceRunning) {
            smsCountTextView.setText(Integer.toString(messagesSent));
            toggleServiceButton.setText(mResources.getString(R.string.stop_service));
        }
        else {
            smsCountTextView.setText("0");
            toggleServiceButton.setText(mResources.getString(R.string.start_service));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startAlarm() {
        //alarm manager setup
        String phoneNumber = phoneNumberEditText.getText().toString();
        String messageText = messageTextEditText.getText().toString();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(MainActivity.this, SmsAlarmReceiver.class);
        myIntent.putExtra("phoneNumber", phoneNumber);
        myIntent.putExtra("messageText", messageText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR, pendingIntent);

        //save user values in prefs
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("resetCounter", true);
        mEditor.putString("phoneNumber", phoneNumber);
        mEditor.putString("messageText", messageText);
        mEditor.apply();

        smsCountTextView.setText("0");
    }

    private void stopAlarm() {
        //unregister alarm
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(MainActivity.this, SmsAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        manager.cancel(pendingIntent);

        pendingIntent.cancel();
    }


}
