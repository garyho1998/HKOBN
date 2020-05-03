package com.geoape.backgroundlocationexample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class BackgroundService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;

    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 10;

    public CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private View layout;
    private ArrayList<GPS> LocationList;
    private GPS lastTarget;
    private Location mLastLocation;
    int countryCode;
    int phoneNumber;
    int waitingTime;
    float distance;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    private GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;
    private GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    public BackgroundService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation = location;
            GPS currentTarget = findGPSMatch(location);
            if(currentTarget!=null && mCountDownTimer==null){
                Log.i(TAG, "currentTarget!=null && mCountDownTimer==null" + waitingTime*60*1000);
                setTime(waitingTime*60*1000);
                startTimer(location);
                lastTarget = currentTarget;
            }else if(currentTarget!=null && mCountDownTimer!=null ){
                Log.i(TAG, "currentTarget!=null && mCountDownTimer!=null");
                if(currentTarget != lastTarget){
                    Log.i(TAG, "currentTarget != lastTarget");
                    resetTimer();
                    pauseTimer();
                    setTime(waitingTime*60*1000);
                    startTimer(location);
                    lastTarget = currentTarget;
                }
            }else if(currentTarget==null && mCountDownTimer!=null){
                Log.i(TAG, "currentTarget==null && mCountDownTimer!=null");
                resetTimer();
                pauseTimer();
                mCountDownTimer = null;
                setNotificationTimer("" ,"AwayFromTarget");
            }else if(currentTarget==null && mCountDownTimer==null){
                Log.i(TAG, "currentTarget==null && mCountDownTimer==null");
            }
            Log.i(TAG, "LocationChanged: "+location);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        countryCode = intent.getIntExtra("countryCode",0);
        phoneNumber = intent.getIntExtra("phoneNumber",0);
        waitingTime = intent.getIntExtra("waitingTime",0);
        distance = (Integer) intent.getIntExtra("distance",0);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");
        LocationList = getLocations();
        startForeground(12345678, getNotification());

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }
    private synchronized void buildGoogleAPIClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
        googleApiClient.connect();
    }
    public void startTracking() {
        System.out.println("startTracking");
        setNotificationTimer("StartTracking","Start");
        connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.i(TAG, "GoogleApiClient connected");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setSmallestDisplacement(1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, mLocationListener);
                if(mLastLocation!=null){
                    Log.i(TAG, "LocationChanged: "+mLastLocation.getLatitude()+ "," + mLastLocation.getLongitude());
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                showToast(R.string.msg_GoogleApiClientConnectionSuspended);
            }
        };
        onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        showToast(R.string.msg_GoogleApiClientConnectionFailed);
                    }
        };
        buildGoogleAPIClient();
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

    }
    public void stopTracking() {
        setNotificationTimer("StopTracking","Stop");
        this.onDestroy();
    }

    private Notification getNotification()
    {
        NotificationChannel channel = new NotificationChannel("HKOBN_Channel_01", "HKOBN_Channel_01", NotificationManager.IMPORTANCE_LOW);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "HKOBN_Channel_01").setAutoCancel(true);
        //builder.setContentText("Not yet timing").setNumber(99);
        return builder.build();
    }

    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }
    private void startTimer(Location location) {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        final int numMessages = 0;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                String timeString = updateCountDownText();
                setNotificationTimer(timeString,"countDown");
                int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
                if(seconds%20==0){
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, mLocationListener);
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        if(mLastLocation ==null){
                            resetTimer();
                            pauseTimer();
                            mCountDownTimer = null;
                            setNotificationTimer("" ,"NoLocationService");
                        }
                        System.out.println(mLastLocation);
                        Log.i(TAG, "requestLocationUpdates: "+ seconds);
                    } catch (java.lang.SecurityException ex) {
                        // Log.i(TAG, "fail to request location update, ignore", ex);
                    } catch (IllegalArgumentException ex) {
                        // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
                    }
                }
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    String sms_txt = "Our user near "+lastTarget.getLocationName() + " " + lastTarget.getLatitude()+", "+lastTarget.getLongitube() +" within " + distance + " meter distance for more than " + waitingTime + " mins.";
                    smsManager.sendTextMessage(Integer.toString(countryCode)+Integer.toString(phoneNumber), null, sms_txt , null, null);
                    setNotificationTimer("Sent SMS to " + Integer.toString(countryCode)+ " " +Integer.toString(phoneNumber) ,"SentSMS");
                }catch(Exception e){
                     Log.i(TAG, "SMS Failed to Send, ignore", e);
                }
            }
        }.start();

        mTimerRunning = true;
    }
    private void setNotificationTimer(String string, String style) {

        NotificationChannel channel = new NotificationChannel("HKOBN_Channel_01", "HKOBN_Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        String notification_title = "";
        String notification_txt = "";

        if(style=="countDown"){
            notification_title = "You are nearing " + lastTarget.getLocationName();
            notification_txt = "Now counting down: " + string;
        }else if(style=="Stop"){
            notification_title = "Stopped Tracking";
            notification_txt = "Stopped GPS Tracking. Timer has been reset";
        }else if(style=="SentSMS"){
            notification_title = "SentSMS ";
            notification_txt = string;
        }else if(style=="AwayFromTarget"){
            notification_title = "Away From Target";
            notification_txt = "You now are away from target. Timer has been reset";
        }else if(style=="Start"){
            notification_title = "Start Tracking";
            notification_txt = "Not near any target";
        }else if(style=="NoLocationService"){
            notification_title = "No Location Service";
            notification_txt = "No Location Service. Timer has been reset ";
        }

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "HKOBN_Channel_01")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(notification_title)
                .setContentText(notification_txt);

        notificationManager.notify(12345678, builder.build());

    }
    private void pauseTimer() {
        if(mCountDownTimer!=null){
            mCountDownTimer.cancel();
        }
        mTimerRunning = false;
    }
    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
    }
    private String updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        return timeLeftFormatted;
       //mTextViewCountDown.setText(timeLeftFormatted);
    }
    public GPS findGPSMatch(Location location){
        for(int i=0; i<LocationList.size();i++){
            GPS temp = LocationList.get(i);
            double tempdistance = distance(location.getLatitude(), location.getLongitude(), temp.getLatitude(), temp.getLongitube(), 'K');
            System.out.println("distance: "+tempdistance + " " + distance/1000);
            if(tempdistance < distance/1000){
                System.out.println("Match! "+ temp.getLatitude()+", "+temp.getLongitube());
                return temp;
            }
        }
        System.out.println("No Match! "+ location.getLatitude()+", "+location.getLongitude());
        return null;
    }
    public ArrayList<GPS> getLocations(){
        ArrayList<GPS> tempList = new ArrayList<GPS>();
        JSONObject jo = loadJSONFromAsset("setting.json");

        try {
            //System.out.println(jo.get("setup"));
            JSONArray ja = (JSONArray)jo.get("locations");
            for(int i=0; i<ja.length();i++){
                JSONObject tempjo = ja.getJSONObject(i);
                GPS temp = new GPS();
                JSONArray gps = (JSONArray)tempjo.get("gps");
                temp.setLatitude(gps.getDouble(0));
                temp.setLongitube(gps.getDouble(1));
                temp.setLocationName(tempjo.getString("type"));
                tempList.add(temp);
                //System.out.println(tempjo.get("gps"));
            }
            //System.out.println(jo.get("setup"));
        }catch(JSONException e){

        }
        return tempList;
    }
    public JSONObject loadJSONFromAsset(String fileName) {
        String json = null;
        JSONObject jo = null;
        try {
            InputStream is = getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            jo = new JSONObject(json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }catch( JSONException ex){
            ex.printStackTrace();
        }
        return jo;
    }
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
