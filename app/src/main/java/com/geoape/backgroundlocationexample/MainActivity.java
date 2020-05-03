package com.geoape.backgroundlocationexample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnStartTracking;
    Button btnStopTracking;
    TextView txtStatus;
    TextView intro;
    public BackgroundService gpsService;
    public boolean mTracking = false;
    boolean have_location_permission = false;
    String countryCode = "";
    String phoneNumber = "";
    String waitingTime = "";
    String distance = "";
    Intent backgroundIntent;
    Bundle backgroundBundle;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private final static String TAG = "MainActivity";
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private LocationManager mLocationManager;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println(location.getLatitude()+", "+location.getLongitude());
            lastLocation = location;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpVariablesAndButtons();
        this.getApplication().startService(backgroundIntent);
        this.getApplication().bindService(backgroundIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationButtonClick() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        System.out.println("LocationEnabled()"+LocationEnabled());
                        if (report.areAllPermissionsGranted() && LocationEnabled()) {
                            System.out.println("AllPermissionsGranted");
                            gpsService.startTracking();
                            mTracking = true;
                            have_location_permission = true;
                            toggleButtons();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Not all permission are granted, cannot process!",
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();
                            waitThread.start();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).onSameThread().check();
    }

    public void stopLocationButtonClick() {
        mTracking = false;
        gpsService.stopTracking();
        if(gpsService.mCountDownTimer!=null){
            gpsService.mCountDownTimer.cancel();
            gpsService.mCountDownTimer = null;
        }
        toggleButtons();
    }

    private void toggleButtons() {
        btnStartTracking.setEnabled(!mTracking);
        btnStopTracking.setEnabled(mTracking);
        txtStatus.setText( (mTracking) ? "Status: TRACKING" : "Status: Stop TRACKING" );
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
                btnStartTracking.setEnabled(true);
                txtStatus.setText("Status: Running Background Service");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                txtStatus.setText("Status: Background Service Disconnected");
                gpsService = null;
            }
        }
    };

    public void setUpVariablesAndButtons(){
        SharedPreferences sp = getSharedPreferences("setting",0);
        countryCode = sp.getString("countryCode","TheDefaultValueIfNoValueFoundOfThisKey");
        phoneNumber = sp.getString("phoneNumber","TheDefaultValueIfNoValueFoundOfThisKey");
        waitingTime = sp.getString("waitingTime","TheDefaultValueIfNoValueFoundOfThisKey");
        distance = sp.getString("distance","TheDefaultValueIfNoValueFoundOfThisKey");

        ImageView imgClick = (ImageView)findViewById(R.id.settingicon);
        btnStartTracking = (Button) findViewById(R.id.btn_start_tracking);
        btnStopTracking = (Button) findViewById(R.id.btn_stop_tracking);
        txtStatus = (TextView) findViewById(R.id.txtStatus) ;
        intro = (TextView) findViewById(R.id.intro) ;

        String introString = "This apps will run in the background and send sms to your emergincy contact ("+countryCode+"-"+phoneNumber+") if you are within "+distance+" meters of a target location for more than "+waitingTime+" mins";
        intro.setText(introString);

        imgClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SetupTabActivity.class));
            }
        });

        btnStartTracking.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startLocationButtonClick();
            }
        });
        btnStopTracking.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                stopLocationButtonClick();
            }
        });
        try{
            int countryCode_int = Integer.parseInt(countryCode);
            int phoneNumber_int = Integer.parseInt(phoneNumber);
            int waitingTime_int = Integer.parseInt(waitingTime);
            int distance_int = Integer.parseInt(distance);
            backgroundIntent = new Intent(this.getApplication(), BackgroundService.class);
            backgroundBundle = new Bundle();
            backgroundBundle.putInt("countryCode", countryCode_int);
            backgroundBundle.putInt("phoneNumber", phoneNumber_int);
            backgroundBundle.putInt("waitingTime", waitingTime_int);
            backgroundBundle.putInt("distance", distance_int);
            backgroundIntent.putExtras(backgroundBundle);
        }catch (Exception e){
            System.out.println(e);
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Some setting parameters cannot convert to integer. Please set up again",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            waitThread.start();
            startActivity(new Intent(MainActivity.this, SetupTabActivity.class));
        }
    }
    final Thread waitThread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(Toast.LENGTH_LONG + 3000); // As I am using LENGTH_LONG in Toast
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public boolean LocationEnabled(){
        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return true;
        } catch(Exception ex) {}
        try {
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return true;
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled) {
            // notify user
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Not all permission are granted, cannot process!",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            return false;
        }
        return false;
    }
}
