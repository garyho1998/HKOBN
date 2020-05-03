package com.geoape.backgroundlocationexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//import pub.devrel.easypermissions.AfterPermissionGranted;
//import pub.devrel.easypermissions.EasyPermissions;


public class WelcomeActivity extends AppCompatActivity {
    Button btn_getStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        btn_getStarted = findViewById(R.id.btn_getStarted);
        btn_getStarted.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                getStartedButtonClick();
            }
        });

    }

    public void getStartedButtonClick(){
        System.out.println("OnClicked");
        SharedPreferences sp = getSharedPreferences("setting",0);
        String countryCode = sp.getString("countryCode","TheDefaultValueIfNoValueFoundOfThisKey");
        String phoneNumber = sp.getString("phoneNumber","TheDefaultValueIfNoValueFoundOfThisKey");
        String duration = sp.getString("waitingTime","TheDefaultValueIfNoValueFoundOfThisKey");
        String distance = sp.getString("distance","TheDefaultValueIfNoValueFoundOfThisKey");

        if(countryCode!="TheDefaultValueIfNoValueFoundOfThisKey"
                && phoneNumber!="TheDefaultValueIfNoValueFoundOfThisKey"
                && duration!="TheDefaultValueIfNoValueFoundOfThisKey"
                && distance!="TheDefaultValueIfNoValueFoundOfThisKey")
        {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        }else{
            startActivity(new Intent(WelcomeActivity.this, SetupTabActivity.class));
        }

    }

}
