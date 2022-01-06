package uk.ac.tees.aad.W9517102.Driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import uk.ac.tees.aad.W9517102.Common.SharedPreference;
import uk.ac.tees.aad.W9517102.DashboardActivity;
import uk.ac.tees.aad.W9517102.R;


public class DriverRegisterFragment extends Fragment {

    EditText emailEt, passwordEt;
    Button btnRegister;
    TextView confirmPasswordEt, loginTv;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference userRef;
    ProgressDialog progressDialog;


    public DriverRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_register, container, false);

        //init all variable
        confirmPasswordEt = view.findViewById(R.id.password2);
        loginTv = view.findViewById(R.id.loginTv);
        btnRegister = view.findViewById(R.id.btnRegister);
        emailEt = view.findViewById(R.id.username);
        passwordEt = view.findViewById(R.id.password);

        progressDialog = new ProgressDialog(getContext());
        //init firebase
        InitFirebase();

        //set click on login button
        loginTv.setOnClickListener(view1 ->
                DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container, new DriverLoginFragment(), null).commit());


        //set click on register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterUser();
            }
        });
        return view;
    }

    //init firebase
    private void InitFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    //register user
    private void RegisterUser() {
        String userEmail = emailEt.getText().toString();
        String userPassword = passwordEt.getText().toString();
        String userCPassword = confirmPasswordEt.getText().toString();

        if (userEmail.isEmpty()) {
            emailEt.setError("Please Select Correct Email");
            emailEt.requestFocus();
        } else if (userPassword.isEmpty()) {
            passwordEt.setError("Please Select Password");
            passwordEt.requestFocus();
        } else if (!userPassword.equals(userCPassword)) {
            confirmPasswordEt.setError("Password not match");
            confirmPasswordEt.requestFocus();
        } else {
            btnRegister.setEnabled(false);

            progressDialog.setMessage("Please wait...!");
            progressDialog.setCancelable(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mUser = mAuth.getCurrentUser();
                        HashMap hashMap = new HashMap();
                        hashMap.put("userId", mUser.getUid());
                        hashMap.put("userType", "driver");
                        hashMap.put("latitude", 0.0);
                        hashMap.put("longitude", 0.0);
                        hashMap.put("status", "available");
                        hashMap.put("image", "Not Found!");
                        hashMap.put("username", "Not Found!");
                        hashMap.put("phone", "Not Found!");

                        userRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    new SharedPreference(getContext()).addLastUser(userEmail, userPassword, "driver");
                                    SendEmailVerification();
                                } else {
                                    btnRegister.setEnabled(true);
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        btnRegister.setEnabled(true);
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendEmailVerification() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Registration Done! Please verification and Login!", Toast.LENGTH_SHORT).show();
                    DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container,new DriverLoginFragment(),null).commit();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}