package com.m.berber.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.m.berber.R;
import com.m.berber.models.User;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail, etPassword;
    private TextInputEditText etShopName, etShopAddress, etWorkingHours;
    private RadioGroup rgRole;
    private RadioButton rbBarber;
    private LinearLayout layoutBarberExtraFields;
    private MaterialButton btnRegister;
    private TextView tvLoginRedirect;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = FirebaseHelper.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etShopName = findViewById(R.id.etShopName);
        etShopAddress = findViewById(R.id.etShopAddress);
        etWorkingHours = findViewById(R.id.etWorkingHours);
        rgRole = findViewById(R.id.rgRole);
        rbBarber = findViewById(R.id.rbBarber);
        layoutBarberExtraFields = findViewById(R.id.layoutBarberExtraFields);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
        progressBar = findViewById(R.id.progressBar);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBarber) {
                layoutBarberExtraFields.setVisibility(View.VISIBLE);
            } else {
                layoutBarberExtraFields.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> performRegistration());
        tvLoginRedirect.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean isBarber = rbBarber.isChecked();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Ad Soyad gerekli");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Telefon numarası gerekli");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("E-posta gerekli");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("En az 6 karakterli şifre gerekli");
            return;
        }

        String shopName = etShopName.getText() != null ? etShopName.getText().toString().trim() : "";
        String shopAddress = etShopAddress.getText() != null ? etShopAddress.getText().toString().trim() : "";
        String workingHours = etWorkingHours.getText() != null ? etWorkingHours.getText().toString().trim() : "";

        if (isBarber && TextUtils.isEmpty(shopName)) {
            etShopName.setError("Dükkan adı gerekli");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        String role = isBarber ? User.ROLE_BARBER : User.ROLE_CUSTOMER;
                        User newUser = new User(fUser.getUid(), name, email, phone, role);
                        if (isBarber) {
                            newUser.setShopName(shopName);
                            newUser.setShopAddress(shopAddress);
                            newUser.setWorkingHours(workingHours.isEmpty() ? "09:00 - 20:00" : workingHours);
                        }

                        firebaseHelper.saveUser(newUser, new FirebaseHelper.OnActionListener() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                btnRegister.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Profil kaydetme hatası (Firestore izni kontrol edilmeli): " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    String errorMessage = e.getMessage();
                    if (e instanceof FirebaseAuthException) {
                        String errorCode = ((FirebaseAuthException) e).getErrorCode();
                        if ("ERROR_OPERATION_NOT_ALLOWED".equalsIgnoreCase(errorCode) || "CONFIGURATION_NOT_FOUND".equalsIgnoreCase(errorCode)) {
                            errorMessage = "Firebase Console'da Email/Password oturum açma seçeneği AÇIK değil!";
                        } else if ("ERROR_EMAIL_ALREADY_IN_USE".equalsIgnoreCase(errorCode)) {
                            errorMessage = "Bu e-posta adresi zaten kullanımda.";
                        } else if ("ERROR_INVALID_EMAIL".equalsIgnoreCase(errorCode)) {
                            errorMessage = "Geçersiz e-posta adresi formatı.";
                        } else if ("ERROR_WEAK_PASSWORD".equalsIgnoreCase(errorCode)) {
                            errorMessage = "Şifre çok zayıf (En az 6 karakter olmalı).";
                        }
                    } else if (errorMessage != null && errorMessage.contains("CONFIGURATION_NOT_FOUND")) {
                        errorMessage = "Firebase Console -> Authentication sekmesinden 'Email/Password' yöntemini açmalısınız.";
                    }

                    Toast.makeText(RegisterActivity.this, "Kayıt başarısız: " + errorMessage, Toast.LENGTH_LONG).show();
                });
    }
}