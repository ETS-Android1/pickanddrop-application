package uk.ac.tees.aad.W9517102.Driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SyncContext;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.ac.tees.aad.W9517102.Common.Customer;
import uk.ac.tees.aad.W9517102.Common.Driver;
import uk.ac.tees.aad.W9517102.Common.Request;
import uk.ac.tees.aad.W9517102.Common.RequestDialog;
import uk.ac.tees.aad.W9517102.Common.Route;
import uk.ac.tees.aad.W9517102.Common.STATIC_CONSTRAINT;
import uk.ac.tees.aad.W9517102.Customer.CustomerRideActivity;
import uk.ac.tees.aad.W9517102.Customer.PlacesActivity;
import uk.ac.tees.aad.W9517102.DashboardActivity;
import uk.ac.tees.aad.W9517102.DirectionAPI.FetchURL;
import uk.ac.tees.aad.W9517102.DirectionAPI.TaskLoadedCallback;
import uk.ac.tees.aad.W9517102.R;

public class DriverHomeActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        LocationListener, TaskLoadedCallback {

    Button searchBar;
    NavigationView navView;
    DrawerLayout drawer_layout;
    LocationRequest mLocationRequest;
    LocationManager locationManager;
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    boolean locationPermissionGranted;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference userRef;
    CircleImageView profileImage;
    TextView usernameTv;
    Switch switchStatus;
    ImageView menuButton;
    Driver driver;
    DatabaseReference requestRef, rideRef, routeLocation;
    ProgressDialog progressDialog;
    LatLng CurrentLatLng;
    Polyline currentPolyline;
    RequestDialog requestDialog;
    Button btnSelectTargetLocation, btnGotoPickup;
    boolean isDrawRouteForRideAdded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        progressDialog = new ProgressDialog(this);
        requestDialog = new RequestDialog(this);
        searchBar = findViewById(R.id.searchBar);
        navView = findViewById(R.id.navView);
        drawer_layout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menu);
        btnGotoPickup = findViewById(R.id.btnGotoPickup);
        btnSelectTargetLocation = findViewById(R.id.btnSelectTargetLocation);
        InitFirebase();
        InitMap();
        View view = navView.inflateHeaderView(R.layout.header_layout);
        usernameTv = view.findViewById(R.id.username);
        switchStatus = view.findViewById(R.id.switchStatus);
        profileImage = view.findViewById(R.id.circleImageView);

        LoadProfileData();
        SyncRide();
        SyncRequestRide();
        StartRide();
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DriverHomeActivity.this, PlacesActivity.class));
            }
        });

        menuButton.setOnClickListener(view1 -> drawer_layout.openDrawer(GravityCompat.START));
        switchStatus.setOnCheckedChangeListener((compoundButton, b) -> {
            HashMap hashMap = new HashMap();
            if (b) {
                hashMap.put("status", "online");
                userRef.child(mUser.getUid()).updateChildren(hashMap);
                switchStatus.setText("Online");
            } else {
                hashMap.put("status", "offline");
                userRef.child(mUser.getUid()).updateChildren(hashMap);
                switchStatus.setText("Offline");
            }
        });
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.logout) {
                mAuth.signOut();
                startActivity(new Intent(DriverHomeActivity.this, DashboardActivity.class));
                finish();
            }

            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(DriverHomeActivity.this, DriverProfileActivity.class));
            }
            return false;
        });
    }


    private void StartRide() {
        routeLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Route route = snapshot1.getValue(Route.class);
                        if (route.getDriverId().equals(mUser.getUid())) {
                            progressDialog.dismiss();
                            if (CurrentLatLng != null) {
                                new FetchURL(DriverHomeActivity.this).execute(getUrl(new LatLng(route.getTargetLocationLat(), route.getTargetLocationLong()), CurrentLatLng), "driving");

                            } else {
                                Toast.makeText(DriverHomeActivity.this, "Enable your location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    if (googleMap != null) {
                        googleMap.clear();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + "walking";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.API_KEY);
    }

    private void SyncRequestRide() {

        rideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Request request = snapshot1.getValue(Request.class);
                        if (request.getDriverID().equals(mUser.getUid())) {
                            if (request.getStatus().equals("approved")) {
                                btnGotoPickup.setVisibility(View.VISIBLE);
                                btnGotoPickup.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        btnGotoPickup.setVisibility(View.GONE);
                                        DrawRouteToPickup(request.getCustomerID());
                                    }
                                });
                            }
                        }
                    }
                } else {
                    btnGotoPickup.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void DrawRouteToPickup(String customerID) {
        if (!isDrawRouteForRideAdded) {
            userRef.child(customerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Customer customer = snapshot.getValue(Customer.class);
                        if (CurrentLatLng != null) {
                            if (googleMap != null) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, 10));
                            }
                            isDrawRouteForRideAdded = true;
                            new FetchURL(DriverHomeActivity.this).execute(getUrl(new LatLng(customer.getLatitude(), customer.getLongitude()), CurrentLatLng), "driving");
                            btnGotoPickup.setVisibility(View.GONE);
                            btnSelectTargetLocation.setVisibility(View.VISIBLE);
                            btnSelectTargetLocation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    rideRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                    Request request = snapshot1.getValue(Request.class);
                                                    if (request.getDriverID().equals(mUser.getUid())) {
                                                        btnSelectTargetLocation.setVisibility(View.GONE);
                                                        HashMap hashMap = new HashMap();
                                                        hashMap.put("status", "pickUpRequest");
                                                        rideRef.child(snapshot1.getRef().getKey()).updateChildren(hashMap);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });

                        } else {

                            Toast.makeText(DriverHomeActivity.this, "Enable your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void SyncRide() {
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Request request = snapshot1.getValue(Request.class);
                        if (request.getDriverID().equals(mUser.getUid())) {
                            requestDialog.ShowDialog(request, snapshot1.getRef().getKey().toString());
                        }
                    }
                } else {
                    requestDialog.DismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void LoadProfileData() {
        userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    driver = snapshot.getValue(Driver.class);
                    Picasso.get().load(driver.getImage()).placeholder(R.drawable.man).into(profileImage);
                    usernameTv.setText(driver.getUsername());

                    if (driver.getStatus().equals("online")) {
                        switchStatus.setChecked(true);
                    } else if (driver.getStatus().equals("offline")) {
                        switchStatus.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void InitMap() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        CurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        HashMap hashMap = new HashMap();
        hashMap.put("latitude", location.getLatitude());
        hashMap.put("longitude", location.getLongitude());
        userRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(DriverHomeActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        getLocationUpdate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                getLocationUpdate();
            } else {
                checkLocationPermission();
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }
        CurrentLocationButtonPosition();
    }

    private void CurrentLocationButtonPosition() {
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 400);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            getLocationUpdate();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    STATIC_CONSTRAINT.FINE_LOCATION);
        }
    }

    private void getLocationUpdate() {
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, DriverHomeActivity.this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, DriverHomeActivity.this);
            } else {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(500);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
                builder.setAlwaysShow(true);
                Task<LocationSettingsResponse> requestTask = LocationServices.getSettingsClient(getApplicationContext())
                        .checkLocationSettings(builder.build());
                requestTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {
                            LocationSettingsResponse result = task.getResult(ApiException.class);
                        } catch (ApiException e) {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            try {
                                resolvableApiException.startResolutionForResult(DriverHomeActivity.this, STATIC_CONSTRAINT.GPS);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STATIC_CONSTRAINT.GPS) {
            switch (STATIC_CONSTRAINT.GPS) {
                case Activity.RESULT_OK:
                    Toast.makeText(this, "GPS is Turned ON", Toast.LENGTH_SHORT).show();

                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "GPS is required to use feature of this app", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case STATIC_CONSTRAINT.FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    googleMap.setMyLocationEnabled(true);
                    getLocationUpdate();
                }
            }
        }
    }

    //init firebase
    private void InitFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Request");
        rideRef = FirebaseDatabase.getInstance().getReference().child("CurrentRide");
        routeLocation = FirebaseDatabase.getInstance().getReference().child("RouteLocation");

    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = googleMap.addPolyline((PolylineOptions) values[0]);
    }
}