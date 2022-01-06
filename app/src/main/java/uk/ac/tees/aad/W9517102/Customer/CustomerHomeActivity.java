package uk.ac.tees.aad.W9517102.Customer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.ac.tees.aad.W9517102.Common.Customer;
import uk.ac.tees.aad.W9517102.Common.Driver;
import uk.ac.tees.aad.W9517102.Common.DriverDetailBookingDialog;
import uk.ac.tees.aad.W9517102.Common.Request;
import uk.ac.tees.aad.W9517102.Common.STATIC_CONSTRAINT;
import uk.ac.tees.aad.W9517102.DashboardActivity;
import uk.ac.tees.aad.W9517102.DirectionAPI.FetchURL;
import uk.ac.tees.aad.W9517102.DirectionAPI.TaskLoadedCallback;
import uk.ac.tees.aad.W9517102.Driver.DriverProfileActivity;
import uk.ac.tees.aad.W9517102.R;

public class CustomerHomeActivity extends AppCompatActivity
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
    DatabaseReference requestRef, rideRef;
    CircleImageView profileImage;
    TextView usernameTv;

    ImageView menuButton;
    public static Customer customer;
    List<Driver> list;
    LatLng CurrentLatLng;
    Polyline currentPolyline;
    Button btnAction;
    String selectedRideDriver;
    Button btnSelectTargetLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);

        searchBar = findViewById(R.id.searchBar);
        navView = findViewById(R.id.navView);
        drawer_layout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menu);
        btnAction = findViewById(R.id.btnAction);
        btnSelectTargetLocation = findViewById(R.id.btnSelectTargetLocation);
        InitFirebase();
        InitMap();

        View view = navView.inflateHeaderView(R.layout.header_layout_customer);
        usernameTv = view.findViewById(R.id.username);
        profileImage = view.findViewById(R.id.circleImageView);
        list = new ArrayList<>();
        LoadProfileData();
        LoadCurrentRide();
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CustomerHomeActivity.this, PlacesActivity.class));
            }
        });

        menuButton.setOnClickListener(view1 -> drawer_layout.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.logout) {
                mAuth.signOut();
                startActivity(new Intent(CustomerHomeActivity.this, DashboardActivity.class));
                finish();
            }

            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(CustomerHomeActivity.this, DriverProfileActivity.class));
            }

            if (item.getItemId() == R.id.RideHistory) {
                startActivity(new Intent(CustomerHomeActivity.this, RideHistoryActivity.class));
            }

            return false;
        });
    }

    private void LoadCurrentRide() {
        rideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Request request = snapshot1.getValue(Request.class);
                        if (request.getCustomerID().equals(mUser.getUid())) {
                            if (request.getStatus().equals("approved")) {
                                selectedRideDriver = request.getDriverID();
                                Intent intent = new Intent(CustomerHomeActivity.this, CustomerRideActivity.class);
                                intent.putExtra("CurrentRideKey", snapshot1.getRef().getKey().toString());
                                intent.putExtra("driverId", request.getDriverID());
                                startActivity(intent);

//                                DrawRouteToPickup(snapshot1.getRef().getKey().toString(), request);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

//    private void DrawRouteToPickup(String key, Request request) {
//        userRef.child(request.getDriverID()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    Driver driver = snapshot.getValue(Driver.class);
//                    if (!alreadyDrawRouteForPickup) {
//                        btnAction.setVisibility(View.VISIBLE);
//                        if (CurrentLatLng != null) {
//                            Toast.makeText(CustomerHomeActivity.this, "Adding Route!", Toast.LENGTH_SHORT).show();
//                            new FetchURL(CustomerHomeActivity.this).execute(getUrl(new LatLng(driver.getLatitude(), driver.getLongitude()), CurrentLatLng), "driving");
//                            alreadyDrawRouteForPickup = true;
//                        } else {
//                            DrawRouteToPickup(key, request);
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//    }


    private void LoadAllTaxi() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    googleMap.clear();
                    list = new ArrayList<>();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Driver dr = snapshot1.getValue(Driver.class);
                        if (dr.getUserType().equals("driver") && dr.getStatus().equals("online")) {
                            list.add(dr);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(dr.getLatitude(), dr.getLongitude()));
                            markerOptions.title("Username:" + dr.getUsername());
                            markerOptions.snippet(dr.getUserId());
                            googleMap.addMarker(markerOptions);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //Load my Profile data and assign on navigation drawer
    private void LoadProfileData() {
        userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    customer = snapshot.getValue(Customer.class);
                    Picasso.get().load(customer.getImage()).placeholder(R.drawable.man).into(profileImage);
                    usernameTv.setText(customer.getUsername());


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
                Toast.makeText(CustomerHomeActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
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
        LoadAllTaxi();

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.infowindowlayout, null);
                ImageView carImage = (ImageView) v.findViewById(R.id.cardImage);
                TextView driverName = (TextView) v.findViewById(R.id.driverName);

                driverName.setText(marker.getTitle());
                return v;
            }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }
        });

        googleMap.setOnInfoWindowClickListener(marker -> {
            LoadDriverInfo(marker.getSnippet());
        });

    }

    private void LoadDriverInfo(String snippet) {
        userRef.child(snippet).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Driver dr = snapshot.getValue(Driver.class);
                    if (CurrentLatLng != null) {
                        new DriverDetailBookingDialog(CustomerHomeActivity.this).ShowDialog(dr, CurrentLatLng);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CurrentLocationButtonPosition() {
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, CustomerHomeActivity.this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, CustomerHomeActivity.this);
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
                                resolvableApiException.startResolutionForResult(CustomerHomeActivity.this, STATIC_CONSTRAINT.GPS);
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
    }

    public float getDistance(double startLat, double startLang, double endLat, double endLang) {
        Location locStart = new Location("");
        locStart.setLatitude(startLat);
        locStart.setLongitude(startLang);
        Location locEnd = new Location("");
        locEnd.setLatitude(endLat);
        locEnd.setLongitude(endLang);

        return locStart.distanceTo(locEnd);
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + "walking";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.API_KEY);
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = googleMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    protected void onResume() {
        LoadCurrentRide();
        super.onResume();

    }
}