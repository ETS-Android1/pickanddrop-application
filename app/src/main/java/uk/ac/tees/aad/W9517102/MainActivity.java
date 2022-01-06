package uk.ac.tees.aad.W9517102;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // wait for 5 second then redirect to Both user page
        Handler handler=new Handler();
        handler.postDelayed(() -> {
            startActivity( new Intent(MainActivity.this, DashboardActivity.class));
            finish();
        }, 5000);
    }
}