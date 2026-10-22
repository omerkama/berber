package com.m.berber.customer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.m.berber.R;

public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationCustomer);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, new CustomerBarberListFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_barbers) {
                selectedFragment = new CustomerBarberListFragment();
            } else if (itemId == R.id.nav_my_appointments) {
                selectedFragment = new CustomerAppointmentsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new CustomerProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.customerFragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}