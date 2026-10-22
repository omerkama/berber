package com.m.berber.barber;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.m.berber.R;

public class BarberMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationBarber);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.barberFragmentContainer, new BarberAppointmentsFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_barber_appointments) {
                selectedFragment = new BarberAppointmentsFragment();
            } else if (itemId == R.id.nav_shop_management) {
                selectedFragment = new BarberShopManagementFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new BarberProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.barberFragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}