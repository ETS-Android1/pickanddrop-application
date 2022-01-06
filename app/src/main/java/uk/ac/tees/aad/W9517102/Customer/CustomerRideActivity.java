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
import android.widget.RelativeLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import uk.ac.tees.aad.W9517102.Common.Customer;
import uk.ac.tees.aad.W9517102.Common.Driver;
import uk.ac.tees.aad.W9517102.Common.History;
import uk.ac.tees.aad.W9517102.Common.Request;
import uk.ac.tees.aad.W9517102.Common.STATIC_CONSTRAINT;
import uk.ac.tees.aad.W9517102.DirectionAPI.FetchURL;
import uk.ac.tees.aad.W9517102.DirectionAPI.TaskLoadedCallback;
import uk.ac.tees.aad.W9517102.R;
import uk.ac.tees.aad.W9517102.Sqlite.DbHistory;

public class CustomerRideActivity extends AppCompatActivity
        implements OnMapReadyCallback, LocationListener,
        TaskLoadedCallback {

    PlacesClient placesClient;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Button inputSearch;
    LocationRequest mLocationRequest;
    LocationManager locationManager;
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    boolean locationPermissionGranted;
    Button btnRoute,btnFinishRide;
    LatLng searchedLocationLatlng;
    LatLng currentLocationLatlng;
    Polyline currentPolyline;
    String CurrentRideKey;
    String driverId;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference userRef;
    DatabaseReference requestRef, rideRef, routeLocation;

    Customer customer;
    Driver driver;
    boolean isAlreadyDrawRoute;
    boolean isRideStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_ride);
        inputSearch = findViewById(R.id.inputSearch);
        btnRoute = findViewById(R.id.btnRoute);
        btnFinishRide = findViewById(R.id.btnFinishRide);

        CurrentRideKey = getIntent().getStringExtra("CurrentRideKey");
        driverId = getIntent().getStringExtra("driverId");

        InitFirebase();
        InitMap();
        LoadUserData();
        LoadPickupStatusData();

        Places.initialize(CustomerRideActivity.this, getString(R.string.API_KEY));
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    searchedLocationLatlng = place.getLatLng();
                    inputSearch.setText(place.getName());
                    if (googleMap != null) {

                        AddRideRoute();
                        btnRoute.setVisibility(View.VISIBLE);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(place.getLatLng());
                        markerOptions.title("Destination Location");
                        googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 10));
                    }
                }
            }
        });

        inputSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placesClient = Places.createClient(CustomerRideActivity.this);
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CustomerRideActivity.this);
                activityResultLauncher.launch(intent);
            }
        });


        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRideStarted) {

                } else {
                    placesClient = Places.createClient(CustomerRideActivity.this);
                    List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CustomerRideActivity.this);
                    activityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void AddRideRoute() {
        if (currentLocationLatlng == null) {
            Toast.makeText(CustomerRideActivity.this, "Enable Current location and Try Again!", Toast.LENGTH_SHORT).show();
        } else if (searchedLocationLatlng == null) {
            Toast.makeText(CustomerRideActivity.this, "Search Current location and Try Again!", Toast.LENGTH_SHORT).show();
        } else {

            HashMap hashMap = new HashMap();
            hashMap.put("targetLocationLat", searchedLocationLatlng.latitude);
            hashMap.put("targetLocationLong", searchedLocationLatlng.longitude);
            hashMap.put("driverId", driver.getUserId());
            hashMap.put("customerId", customer.getUserId());

            String key=routeLocation.push().getKey().toString();
            routeLocation.child(key).updateChildren(hashMap);
            Toast.makeText(CustomerRideActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
            new FetchURL(CustomerRideActivity.this).execute(getUrl(currentLocationLatlng, searchedLocationLatlng), "driving");

            btnFinishRide.setVisibility(View.VISIBLE);
            btnFinishRide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    routeLocation.child(key).removeValue();
                    AddSqliteDatabase();
                }
            });
        }
    }

    private void AddSqliteDatabase() {
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("d MMM yyyy");
        String cDay = dateFormat1.format(new Date());

        startActivity(new Intent(CustomerRideActivity.this,CustomerHomeActivity.class));
        Toast.makeText(CustomerRideActivity.this, "Save History Sqlite Database ", Toast.LENGTH_SHORT).show();
        DbHistory dbHistory=new DbHistory(CustomerRideActivity.this);
        dbHistory.addHistory(new History(driver.getUsername(),customer.getUsername(),cDay,currentLocationLatlng.latitude,currentLocationLatlng.longitude,searchedLocationLatlng.latitude,searchedLocationLatlng.longitude));
        finish();
    }

    private void LoadPickupStatusData() {
        rideRef.child(CurrentRideKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Request request = snapshot.getValue(Request.class);
                    if (request.getStatus().equals("pickUpRequest"))
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(CustomerRideActivity.this);
                        builder.setMessage("Please confirm below when you Picked Taxi and Select Target Location!");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                rideRef.child(CurrentRideKey).removeValue();
                            }
                        });
                        builder.create();
                        builder.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    private void LoadUserData() {
        userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    customer = snapshot.getValue(Customer.class);
                    LoadDriverData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void LoadDriverData() {
        userRef.child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    driver = snapshot.getValue(Driver.class);
                    DrawPickRoute();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void DrawPickRoute() {
        if (!isAlreadyDrawRoute) {
            if (driver != null && customer != null) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(customer.getLatitude(), customer.getLongitude()), 10));
                }
                Toast.makeText(CustomerRideActivity.this, "Driver is coming to Pickup!", Toast.LENGTH_SHORT).show();
                new FetchURL(CustomerRideActivity.this).execute(getUrl(new LatLng(driver.getLatitude(), driver.getLongitude()), new LatLng(customer.getLatitude(), customer.getLongitude())), "driving");
                isAlreadyDrawRoute = true;
            } else {
                DrawPickRoute();
            }
        }
    }

    private void InitMap() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocationLatlng = new LatLng(location.getLatitude(), location.getLongitude());
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

    private void getLocationUpdate() {
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, CustomerRideActivity.this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, CustomerRideActivity.this);
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
                                resolvableApiException.startResolutionForResult(CustomerRideActivity.this, STATIC_CONSTRAINT.GPS);
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
                    Toast.makeText(this, "GPS Module is Turned ON", Toast.LENGTH_SHORT).show();

                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "GPS Module is required to use feature of this app", Toast.LENGTH_SHORT).show();
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

    private void CurrentLocationButtonPosition() {
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 400);
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
}