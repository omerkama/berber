// Bismillahir Rahmanir Rahim
// El Hamdu Lillahi Rabbul Alemin
// Esselatu vesselamu ala rasulillah
// Allahumme salli ala seyyidina Muhammedin
// Allah u Ekber, Allahu Ekber, Allahu Ekber
// La ilahe illAllah, Allahu Ekber,
// SubhanAllah, Elhamdulillah, La ilahe illAllah, Allahu Ekber
// Estaxfurullah El Azim
// Allahu Ekber ve Lillahil Hamd
package com.m.berber;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.m.berber.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}