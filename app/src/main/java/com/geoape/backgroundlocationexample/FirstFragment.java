package com.geoape.backgroundlocationexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;

public class FirstFragment extends Fragment {

    public FirstFragment() {
// Required empty public constructor
    }

    Button btn_getStarted;
    View inflatedView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        this.inflatedView = inflater.inflate(R.layout.activity_setup1, container, false);
        btn_getStarted = (Button) inflatedView .findViewById(R.id.btn_next1);
        btn_getStarted.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                nextButtOnClick();
            }
        });
        return inflatedView ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void nextButtOnClick(){
        System.out.println("OnClicked");
        EditText countryCode_et =  (EditText) inflatedView.findViewById(R.id.countryCode);
        String countryCode = countryCode_et.getText().toString();
        EditText phoneNumber_et =  (EditText) inflatedView.findViewById(R.id.phoneNumber);
        String phoneNumber = phoneNumber_et.getText().toString();
        if(!isNullOrEmpty(countryCode) && !isNullOrEmpty(phoneNumber)){
            SharedPreferences settings = getActivity().getSharedPreferences("setting", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("withContact","true");
            editor.putString("countryCode",countryCode);
            editor.putString("phoneNumber",phoneNumber);
            editor.commit();
            ((SetupTabActivity)getActivity()).selectTab(1);
        }else{
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    "All text fields are required",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }
    }
    public static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }
}