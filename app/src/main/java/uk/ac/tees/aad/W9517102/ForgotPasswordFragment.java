package uk.ac.tees.aad.W9517102;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import uk.ac.tees.aad.W9517102.Customer.CustomerLoginFragment;
import uk.ac.tees.aad.W9517102.Driver.DriverLoginFragment;


public class ForgotPasswordFragment extends Fragment {

    private String userType;
    EditText inputEmail;
    Button btnSend;
    ProgressDialog progressDialog;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userType = getArguments().getString("userType");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        inputEmail = view.findViewById(R.id.inputEmail);
        btnSend = view.findViewById(R.id.btnSend);
        progressDialog = new ProgressDialog(getContext());
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                if (email.isEmpty()) {
                    inputEmail.setError("Please Select Email");
                    inputEmail.requestFocus();
                } else {
                    ResetPassword(email);
                }
            }
        });
        return view;

    }

    private void ResetPassword(String email) {
        progressDialog.setMessage("Please wait!...");
        progressDialog.show();
        ;
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            if (userType.equals("driver")) {
                                DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container, new DriverLoginFragment(), null).commit();
                            }
                            if (userType.equals("customer")) {
                                DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container, new CustomerLoginFragment(), null).commit();
                            }
                            Toast.makeText(getContext(), "Check Your Email", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}