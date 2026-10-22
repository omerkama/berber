package com.m.berber.barber;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.m.berber.R;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.auth.LoginActivity;
import com.m.berber.models.User;

public class BarberProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileRole, tvProfileEmail, tvProfilePhone;
    private MaterialButton btnLogout;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        firebaseHelper = FirebaseHelper.getInstance();

        loadProfile();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadProfile() {
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (getContext() == null || user == null) return;
                tvProfileName.setText(user.getShopName() != null ? user.getShopName() : user.getName());
                tvProfileRole.setText("Hizmet Veren (Berber)");
                tvProfileEmail.setText("E-posta: " + user.getEmail());
                tvProfilePhone.setText("Telefon: " + (user.getPhone() != null ? user.getPhone() : "-"));
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Profil yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}