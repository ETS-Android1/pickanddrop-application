package uk.ac.tees.aad.W9517102;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import uk.ac.tees.aad.W9517102.Common.SelectUser;
import uk.ac.tees.aad.W9517102.Common.UsersDialog;
import uk.ac.tees.aad.W9517102.Driver.DriverLoginFragment;
import uk.ac.tees.aad.W9517102.Customer.CustomerLoginFragment;

public class DashboardActivity extends AppCompatActivity implements SelectUser {

    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbaord);
        fragmentManager = getSupportFragmentManager();
        new UsersDialog(DashboardActivity.this);

    }

    @Override
    public void selectUser(String user) {
        if (user.equals("rider")) {
            if (findViewById(R.id.container) != null) {
                fragmentManager.beginTransaction().add(R.id.container, new CustomerLoginFragment(), null).commit();

            }
        }

        if (user.equals("driver")) {
            if (findViewById(R.id.container) != null) {
                fragmentManager.beginTransaction().add(R.id.container, new DriverLoginFragment(), null).commit();
            }
        }
    }


}