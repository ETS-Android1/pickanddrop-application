package uk.ac.tees.aad.W9517102.Common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.logging.LogRecord;

import uk.ac.tees.aad.W9517102.Customer.CustomerHomeActivity;
import uk.ac.tees.aad.W9517102.DashboardActivity;
import uk.ac.tees.aad.W9517102.MainActivity;
import uk.ac.tees.aad.W9517102.R;

public class DriverDetailBookingDialog {
    Context context;
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference requestRef, rideRef;
    ProgressDialog progressDialog;
    Handler handler;


    public DriverDetailBookingDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Request");
        rideRef = FirebaseDatabase.getInstance().getReference().child("CurrentRide");
        progressDialog = new ProgressDialog(context);
        handler = new Handler();

    }

    public void ShowDialog(Driver driver, LatLng CurrentLatlng) {
        dialog.setContentView(R.layout.layout_dialog_driver_detail);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btnBookingRequest = dialog.findViewById(R.id.btnBookingRequest);
        TextView driverName = dialog.findViewById(R.id.driverName);
        TextView distance = dialog.findViewById(R.id.distance);
        driverName.setText(driver.getUsername());
        if (CurrentLatlng != null) {
            float d = getDistance(driver.getLatitude(), driver.getLongitude(), CurrentLatlng.longitude, CurrentLatlng.longitude);
            if (d > 10) {
                distance.setText(d / 1000 + " km away from me");
            } else {
                distance.setText(d + " m away from me");
            }
        } else {
            distance.setText("Distance can't calculated when current location is not found!,Enable location and click again");
        }
        btnBookingRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap hashMap = new HashMap();
                hashMap.put("customerID", mUser.getUid());
                hashMap.put("customerName", CustomerHomeActivity.customer.getUsername());
                hashMap.put("driverID", driver.getUserId());
                hashMap.put("driverName", driver.getUsername());
                hashMap.put("status", "requesting");
                String key = requestRef.push().getKey().toString();
                requestRef.child(key).updateChildren(hashMap);

                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Please wait, Driver will respond!.");
                progressDialog.show();


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        requestRef.child(key).removeValue();
                    }
                }, 20000);

                //we get this key from driver and save new ref against this
                rideRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Request request = snapshot.getValue(Request.class);
                            if (request.getStatus().equals("approved")) {
                                progressDialog.dismiss();
                                dialog.dismiss();
                                Toast.makeText(context, "Approved", Toast.LENGTH_SHORT).show();
                            }
                            if (request.getStatus().equals("declined")) {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Driver declined request", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("Driver Decline your ride request!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        rideRef.child(key).removeValue();
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        dialog.show();
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
}
