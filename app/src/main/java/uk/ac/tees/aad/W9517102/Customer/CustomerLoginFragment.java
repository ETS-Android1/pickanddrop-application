package uk.ac.tees.aad.W9517102.Customer;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

import uk.ac.tees.aad.W9517102.Common.SharedPreference;
import uk.ac.tees.aad.W9517102.DashboardActivity;
import uk.ac.tees.aad.W9517102.ForgotPasswordFragment;
import uk.ac.tees.aad.W9517102.R;


public class CustomerLoginFragment extends Fragment {
    private static final int REQUEST_CODE = 10101;
    EditText emailEt, passwordEt;
    Button btnLogin;
    TextView forgotPasswordTv, registerTv;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference userRef;
    ProgressDialog progressDialog;
    ImageView fingerPrintIv;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    Executor executor;



    public CustomerLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rirder_login, container, false);
        forgotPasswordTv = view.findViewById(R.id.forgotPasswordTv);
        registerTv = view.findViewById(R.id.registerTv);
        btnLogin = view.findViewById(R.id.btnLogin);
        emailEt = view.findViewById(R.id.username);
        passwordEt = view.findViewById(R.id.password);
        fingerPrintIv = view.findViewById(R.id.fingerPrintIv);
        progressDialog = new ProgressDialog(getContext());

        InitFirebase();
        FingerPrint();

        registerTv.setOnClickListener(view12 -> DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container, new CustomerRegisterFragment(), null).commit());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = emailEt.getText().toString();
                String userPassword = passwordEt.getText().toString();

                if (userEmail.isEmpty()) {
                    emailEt.setError("Please Select Correct Email");
                    emailEt.requestFocus();
                } else if (userPassword.isEmpty()) {
                    passwordEt.setError("Please Select Password");
                    passwordEt.requestFocus();
                } else {
                    btnLogin.setEnabled(false);
                    LoginUser(userEmail, userPassword);
                }
            }
        });
        fingerPrintIv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                FingerPrintAuthentication();
            }
        });

        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ForgotPasswordFragment fragment = new ForgotPasswordFragment();
                Bundle args = new Bundle();
                args.putString("userType", "customer");

                fragment.setArguments(args);
                DashboardActivity.fragmentManager.beginTransaction().replace(R.id.container,fragment,null).commit();
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    private void FingerPrintAuthentication() {
        String userEmail = new SharedPreference(getContext()).getLastUserEmail();
        String userPassword = new SharedPreference(getContext()).getLastUserPassword();
        BiometricManager biometricManager = BiometricManager.from(getContext());
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }

        executor = ContextCompat.getMainExecutor(getContext());
        biometricPrompt = new BiometricPrompt(getActivity(),
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (userEmail != null && userPassword != null) {
                    LoginUser(userEmail, userPassword);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);

    }

    private void LoginUser(String userEmail, String userPassword) {
        btnLogin.setEnabled(false);
        progressDialog.setMessage("Please wait...!");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    CheckValidation( userEmail,  userPassword);
                } else {
                    btnLogin.setEnabled(true);
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //init firebase
    private void InitFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void isAvailableUser(String userEmail, String userPassword) {
        mUser = mAuth.getCurrentUser();
        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("userType")) {
                    if (snapshot.child("userType").getValue().toString().equals("customer")) {
                        progressDialog.dismiss();
                        new SharedPreference(getContext()).addLastUser(userEmail, userPassword, "customer");
                        Intent intent = new Intent(getContext(), CustomerHomeActivity.class);
                        startActivity(intent);
                        Toast.makeText(getContext(), "Login Done!", Toast.LENGTH_SHORT).show();
                    } else {
                        btnLogin.setEnabled(true);
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "This is Driver email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btnLogin.setEnabled(true);
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "User not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnLogin.setEnabled(true);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void FingerPrint() {
        if (new SharedPreference(getContext()).getLastUserType().equals("customer")) {
            fingerPrintIv.setVisibility(View.VISIBLE);
        } else {
            fingerPrintIv.setVisibility(View.GONE);
        }
    }

    private void CheckValidation(String userEmail, String userPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean emailVerified = user.isEmailVerified();
        if (!emailVerified) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Email is not Verified", Toast.LENGTH_SHORT).show();
        }else
        {
            isAvailableUser(userEmail,userPassword);
        }
    }
}