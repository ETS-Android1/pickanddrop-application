package uk.ac.tees.aad.W9517102.Customer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.ac.tees.aad.W9517102.Common.Customer;
import uk.ac.tees.aad.W9517102.Common.STATIC_CONSTRAINT;
import uk.ac.tees.aad.W9517102.R;

public class CustomerProfileActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    EditText inputEmail;
    EditText inputUsername;
    EditText inputPhone;
    Button btnUpdate;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference userRef;
    ProgressDialog progressDialog;
    StorageReference StorageRef;
    Uri uri = null;
    Customer customer;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        circleImageView = findViewById(R.id.circleImageView);
        inputEmail = findViewById(R.id.inputEmail);
        inputEmail = findViewById(R.id.inputEmail);
        inputUsername = findViewById(R.id.inputUsername);
        inputPhone = findViewById(R.id.inputPhone);
        btnUpdate = findViewById(R.id.btnUpdate);
        back = findViewById(R.id.back);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...!");
        progressDialog.setCancelable(false);

        InitFirebase();
        LoadProfileData();
        btnUpdate.setOnClickListener(view -> UpdateProfile());
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(CustomerProfileActivity.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(STATIC_CONSTRAINT.PICK_IMAGE_REQUEST);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    //init firebase
    private void InitFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRef = FirebaseStorage.getInstance().getReference().child("StorageImages");
    }


    private void LoadProfileData() {
        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    customer=snapshot.getValue(Customer.class);
                    Picasso.get().load(customer.getImage()).placeholder(R.drawable.man).into(circleImageView);
                    inputUsername.setText(customer.getUsername());
                    inputEmail.setText(mUser.getEmail());
                    inputPhone.setText(customer.getPhone());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpdateProfile() {
        String username = inputUsername.getText().toString();
        String phone = inputPhone.getText().toString();

        if (username.isEmpty()) {
            inputEmail.setError("Select Email");
            inputEmail.requestFocus();
        } else if (phone.isEmpty()) {
            inputEmail.setError("Select Phone");
            inputEmail.requestFocus();
        }

        if (uri != null) {
            progressDialog.show();
            StorageRef.child(mUser.getUid()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        StorageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("image", uri.toString());
                                hashMap.put("username", username);
                                hashMap.put("phone", phone);
                                userRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(CustomerProfileActivity.this, "Profile is Updated", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        LoadProfileData();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(CustomerProfileActivity.this, "" + task1.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        } else {

            progressDialog.show();
            HashMap hashMap = new HashMap();
            hashMap.put("username", username);
            hashMap.put("phone", phone);
            userRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CustomerProfileActivity.this, "Profile is Updated without Image", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(CustomerProfileActivity.this, "" + task1.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STATIC_CONSTRAINT.PICK_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getData();
                circleImageView.setImageURI(uri);
            }
        }
    }
}