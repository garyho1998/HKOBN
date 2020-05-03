package com.geoape.backgroundlocationexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ThirdFragment extends Fragment {

    public ThirdFragment() {
// Required empty public constructor
    }

    Button btn_getStarted;
    View inflatedView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        this.inflatedView = inflater.inflate(R.layout.activity_setup3, container, false);
        btn_getStarted = (Button) inflatedView .findViewById(R.id.btn_next3);
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
        EditText distance_et =  (EditText) inflatedView.findViewById(R.id.distance);
        String distance = distance_et.getText().toString();
        EditText waitingTime_et =  (EditText) inflatedView.findViewById(R.id.waitingTime);
        String waitingTime = waitingTime_et.getText().toString();

        if(!isNullOrEmpty(waitingTime) && !isNullOrEmpty(distance) ){
            if(waitingTime!="0"){
                SharedPreferences settings = getActivity().getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("withSensitivity","true");
                editor.putString("distance",distance);
                editor.putString("waitingTime",waitingTime);
                editor.commit();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }else{
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "All text fields are required",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
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
