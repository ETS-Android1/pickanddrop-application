package uk.ac.tees.aad.W9517102.Common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import uk.ac.tees.aad.W9517102.R;

public class RequestDialog {
    Context context;
    Dialog dialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference requestRef,rideRef;


    public RequestDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Request");
        rideRef = FirebaseDatabase.getInstance().getReference().child("CurrentRide");
    }
    public void ShowDialog(Request request ,String key){

        dialog.setContentView(R.layout.layout_dialog_request);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnBookingRequest = dialog.findViewById(R.id.btnBookingRequest);
        Button btnDeclineRequest = dialog.findViewById(R.id.btnDeclineRequest);

        btnBookingRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap hashMap=new HashMap();
                hashMap.put("customerID",request.getCustomerID());
                hashMap.put("customerName",request.getCustomerName());
                hashMap.put("driverID",request.getDriverID());
                hashMap.put("driverName",request.getDriverName());
                hashMap.put("status","approved");
                rideRef.child(key).updateChildren(hashMap);
                requestRef.child(key).removeValue();
                dialog.dismiss();
            }
        });

        btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap hashMap=new HashMap();
                hashMap.put("customerID",request.getCustomerID());
                hashMap.put("customerName",request.getCustomerName());
                hashMap.put("driverID",request.getDriverID());
                hashMap.put("driverName",request.getDriverName());
                hashMap.put("status","declined");
                rideRef.child(key).updateChildren(hashMap);
                requestRef.child(key).removeValue();
                dialog.dismiss();
            }
        });

        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void DismissDialog(){
        dialog.dismiss();
    }
}
