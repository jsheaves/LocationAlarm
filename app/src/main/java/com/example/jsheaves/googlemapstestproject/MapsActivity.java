package com.example.jsheaves.googlemapstestproject;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    Button createReminderButton;
    EditText reminderInput;
    EditText radiusInput;
    Handler handler = new Handler();
    LatLng setLocation;
    Circle circle;
    AccessibilityService context;
    private static final int NOTE_ID = 100;
    String getReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createReminderButton = (Button) findViewById(R.id.createReminderButton);
        createReminderButton.setOnClickListener(clickHandler);
        reminderInput = (EditText)findViewById(R.id.reminderText);
        radiusInput = (EditText)findViewById(R.id.radiusText);
    }

    View.OnClickListener clickHandler = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            try {
                runInit();
                runnable.run();
            }
            catch(Exception ex)
            {
                Context context = getApplicationContext();
                CharSequence text = "Please enter a valid reminder text and radius";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateLocation();

            handler.postDelayed(this, 5000);
        }
    };

    public void runInit() {

        Integer intLoopControlVariable = 0;
        getReminder = reminderInput.getText().toString();
        Integer getRadius = Integer.parseInt(radiusInput.getText().toString());

        Location myLocation = mMap.getMyLocation();
        if (myLocation != null) {
            mMap.clear();
            double dLatitude = myLocation.getLatitude();
            double dLongitude = myLocation.getLongitude();
            setLocation = new LatLng(dLatitude, dLongitude);
            circle = mMap.addCircle(new CircleOptions()
                    .center(setLocation)
                    .radius(getRadius)
                    .strokeColor(Color.RED)
                    .fillColor(0x5500ff00));

            mMap.addMarker(new MarkerOptions()
                    .position(setLocation)
                    .title(getReminder));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(setLocation));

            Context context = getApplicationContext();
            CharSequence text = "Your notifier has been successfully set!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Unable to retrieve your current location";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    public void updateLocation()
    {
        Location myLocation = mMap.getMyLocation();
        double dLatitude = myLocation.getLatitude();
        double dLongitude = myLocation.getLongitude();
        float[] distance = new float[2];

        LatLng currentLocation = new LatLng(dLatitude, dLongitude);

        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if(distance[0] <= circle.getRadius()) {

        }

        else
        {
            Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();

                createNotification();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent launchIntent = new Intent(getApplicationContext(), MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this);
        //Set notification information
        builder.setSmallIcon(R.drawable.notification_icon)
                .setTicker("Something Happened")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("Ding!")
                .setContentText("It's time to " + getReminder + "!")
                .setContentIntent(contentIntent);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
        style.setSummaryText("Location Reminder");
        style.addLine("It's time to " + getReminder + "!");

        Notification note = style.build();
        manager.notify(NOTE_ID, note);
    }
}

