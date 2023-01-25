package com.example.e_junk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragmentDriver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragmentDriver extends Fragment implements OnMapReadyCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LocationFragmentDriver() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragmentDriver.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationFragmentDriver newInstance(String param1, String param2) {
        LocationFragmentDriver fragment = new LocationFragmentDriver();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FloatingActionButton trackBTN;
    FirebaseAuth ejunkAuth;
    FirebaseUser ejunkUser;
    UserHelperClass userhelperClass;
    LocationHelperClass locationHelperClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location_driver, container, false);

        //this is the mapp
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        trackBTN = (FloatingActionButton) view.findViewById(R.id.trackBTN);
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        trackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Start/Stop Sharing Location")
                        .setMessage("By clicking \"START,\" other users will be able to see your location! After marking the entire schedule as completed, make sure click \"STOP\".")
                        .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    enableLocationSettings();
                                } else {
                                    requestAppPermissions();
                                }
                            }
                        })
                        .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stopSharingLocation();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create().show();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
       setDriversLocation(googleMap);
    }

    private void setDriversLocation(final GoogleMap googleMap) {
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();
        String UserID = ejunkUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userhelperClass = snapshot.getValue(UserHelperClass.class);
                if(userhelperClass != null) {
                    String barangay = userhelperClass.getBarangay();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("location");
                    reference.child(barangay).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                locationHelperClass = dataSnapshot.getValue(LocationHelperClass.class);
                                googleMap.clear();
                                LatLng driverLoc = new LatLng(locationHelperClass.getLatitude(), locationHelperClass.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(driverLoc).title(locationHelperClass.getName()));
                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(driverLoc));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLoc, 18f));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FirebaseError", error.getDetails());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void stopSharingLocation(){
        getActivity().stopService(new Intent(getActivity(), ForegroundService.class));
        ejunkAuth = FirebaseAuth.getInstance();
        ejunkUser = ejunkAuth.getCurrentUser();
        String UserID = ejunkUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ejunk_user");
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userhelperClass = snapshot.getValue(UserHelperClass.class);
                if(userhelperClass != null) {
                    String barangay = userhelperClass.getBarangay();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("location");
                    reference.child(barangay).child(UserID).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void requestAppPermissions() {

        Dexter.withContext(getActivity())
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if(multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(getActivity(), "Location is running", Toast.LENGTH_SHORT).show();
                            getActivity().startService(new Intent(getActivity(), ForegroundService.class));
                        }
                        if(multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                    }
                }).onSameThread().check();
    }

    private void showSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Need Permissions")
                .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();

    }

    protected void enableLocationSettings() {

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        LocationServices
                .getSettingsClient(getActivity())
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(getActivity(), (LocationSettingsResponse response) -> {
                    // startUpdatingLocation(...);
                })

                .addOnFailureListener(getActivity(), ex -> {

                    if (ex instanceof ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(getActivity(), 123);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }

                });

    }

}