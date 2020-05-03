package com.geoape.backgroundlocationexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SecondFragment extends Fragment {

    public SecondFragment() {
// Required empty public constructor
    }

    Button btn_next2;
    View inflatedView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        this.inflatedView = inflater.inflate(R.layout.activity_setup2, container, false);
        btn_next2 = (Button) inflatedView .findViewById(R.id.btn_next2);
        btn_next2.setOnClickListener(new View.OnClickListener(){
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
        CheckBox checkBox1 =  (CheckBox) inflatedView.findViewById(R.id.checkBox1);
        boolean boolean1 = checkBox1.isChecked();
        CheckBox checkBox2 =  (CheckBox) inflatedView.findViewById(R.id.checkBox2);
        boolean boolean2 = checkBox2.isChecked();
        EditText lad1_et =  (EditText) inflatedView.findViewById(R.id.lad2);
        String lad1 = lad1_et.getText().toString();
        EditText long1_et =  (EditText) inflatedView.findViewById(R.id.long2);
        String long1 = long1_et.getText().toString();
        EditText lad2_et =  (EditText) inflatedView.findViewById(R.id.lad2);
        String lad2 = lad2_et.getText().toString();
        EditText long2_et =  (EditText) inflatedView.findViewById(R.id.long2);
        String long2 = long2_et.getText().toString();

        if(boolean1 || boolean2){
            ((SetupTabActivity)getActivity()).selectTab(2);
        }else{
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    "Please choose at least one target",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }
    }

}