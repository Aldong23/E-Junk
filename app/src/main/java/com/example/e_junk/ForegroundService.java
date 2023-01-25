package com.example.e_junk;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.squareup.picasso.Picasso;


public class ForegroundService extends Service {

    private final IBinder mBinder = new MyBinder();
    private static final String CHANNEL_ID = "2";
    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    UserHelperClass userhelperClass;
    LocationHelperClass locationHelperClass;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        requestLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(ForegroundService.this, "Location is stopping", Toast.LENGTH_SHORT).show();
        stopSelf();
        ProcessPhoenix.triggerRebirth(this);
    }

    private void buildNotification() {
        String stop = "stop";
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Location tracking is working")
                .setOngoing(true)
                .setContentIntent(broadcastIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(false);
            channel.setDescription("Location tracking is working");
            channel.setSound(null, null);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

        }
        startForeground(1, builder.build());

    }

    private void requestLocationUpdates() {
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();

        String UserID = ejunkUser.getUid();

        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    double Latitude = locationResult.getLastLocation().getLatitude();
                    double Longitude = locationResult.getLastLocation().getLongitude();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
                    reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userhelperClass = snapshot.getValue(UserHelperClass.class);
                            if(userhelperClass != null){
                                String username = userhelperClass.getUsername();
                                String barangay = userhelperClass.getBarangay();
                                getDriverLocation(UserID, username, barangay, Latitude, Longitude);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }, null);

        } else {

            stopSelf();

        }

    }

    private void getDriverLocation(String userID, String username, String barangay, double latitude, double longitude) {
        locationHelperClass = new LocationHelperClass(userID, username, barangay, latitude, longitude);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("location");
        reference.child(barangay).child(userID).setValue(locationHelperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }else{

                }
            }
        });
    }

    public class MyBinder extends Binder {

        public ForegroundService getService() {
            return ForegroundService.this;

        }

    }

}