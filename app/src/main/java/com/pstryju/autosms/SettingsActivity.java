package com.pstryju.autosms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    Switch mBlindModeSwitch;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences("AutoSms", MODE_PRIVATE);
        boolean blindMode = mSharedPreferences.getBoolean("blindMode", false);
        if(blindMode)
            setTheme(R.style.blindTheme);
        setContentView(R.layout.activity_settings);


        mBlindModeSwitch = (Switch) findViewById(R.id.blind_mode_switch);
        mBlindModeSwitch.setChecked(blindMode);
        mBlindModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(b)
                    editor.putBoolean("blindMode", true);
                else
                    editor.putBoolean("blindMode", false);
                editor.apply();
            }
        });



    }

}
