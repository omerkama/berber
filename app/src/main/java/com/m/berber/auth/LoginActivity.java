package com.m.berber.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.m.berber.R;
import com.m.berber.barber.BarberMainActivity;
import com.m.berber.customer.CustomerMainActivity;
import com.m.berber.models.User;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleLogin, btnPhoneLogin;
    private TextView tvRegisterRedirect;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // Phone Auth variables
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseHelper = FirebaseHelper.getInstance();

        // Check if user is already logged in
        if (firebaseHelper.isUserLoggedIn()) {
            checkUserRoleAndNavigate();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnPhoneLogin = findViewById(R.id.btnPhoneLogin);
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);
        progressBar = findViewById(R.id.progressBar);

        // Setup Google Sign In
        setupGoogleSignIn();

        btnLogin.setOnClickListener(v -> performLogin());
        btnGoogleLogin.setOnClickListener(v -> launchGoogleSignIn());
        btnPhoneLogin.setOnClickListener(v -> showPhoneAuthDialog());

        tvRegisterRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("E-posta gerekli");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Şifre gerekli");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkUserRoleAndNavigate())
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    handleAuthError(e);
                });
    }

    // Google Sign-In Setup & Launcher
    private void setupGoogleSignIn() {
        int webClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        String webClientId = webClientIdRes != 0 ? getString(webClientIdRes) : "";

        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        if (!webClientId.isEmpty()) {
            gsoBuilder.requestIdToken(webClientId);
        }

        GoogleSignInOptions gso = gsoBuilder.build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null && account.getIdToken() != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else if (account != null) {
                                // If idToken is null (e.g. webClientId not set), handle signed in Google account
                                checkUserRoleAndNavigate();
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google ile giriş başarısız: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void launchGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> checkUserRoleAndNavigate())
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    handleAuthError(e);
                });
    }

    // Phone Auth Dialog
    private void showPhoneAuthDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_phone_auth, null);
        LinearLayout layoutStepPhone = dialogView.findViewById(R.id.layoutStepPhone);
        LinearLayout layoutStepCode = dialogView.findViewById(R.id.layoutStepCode);
        EditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);
        EditText etSmsCode = dialogView.findViewById(R.id.etSmsCode);
        MaterialButton btnSendCode = dialogView.findViewById(R.id.btnSendCode);
        MaterialButton btnVerifyCode = dialogView.findViewById(R.id.btnVerifyCode);
        ProgressBar progressBarPhone = dialogView.findViewById(R.id.progressBarPhone);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSendCode.setOnClickListener(v -> {
            String phone = etPhoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(phone) || phone.length() < 10) {
                etPhoneNumber.setError("Geçerli bir telefon numarası giriniz (Örn: +905551234567)");
                return;
            }

            progressBarPhone.setVisibility(View.VISIBLE);
            btnSendCode.setEnabled(false);

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            progressBarPhone.setVisibility(View.GONE);
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                    .addOnSuccessListener(authResult -> {
                                        dialog.dismiss();
                                        checkUserRoleAndNavigate();
                                    })
                                    .addOnFailureListener(e -> handleAuthError(e));
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            progressBarPhone.setVisibility(View.GONE);
                            btnSendCode.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "SMS gönderme hatası: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            progressBarPhone.setVisibility(View.GONE);
                            verificationId = verId;
                            layoutStepPhone.setVisibility(View.GONE);
                            layoutStepCode.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "SMS doğrulama kodu gönderildi.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        btnVerifyCode.setOnClickListener(v -> {
            String code = etSmsCode.getText().toString().trim();
            if (TextUtils.isEmpty(code) || code.length() < 6) {
                etSmsCode.setError("6 haneli kodu giriniz");
                return;
            }

            if (verificationId == null) {
                Toast.makeText(this, "Doğrulama kodu oturumu geçersiz.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarPhone.setVisibility(View.VISIBLE);
            btnVerifyCode.setEnabled(false);

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        progressBarPhone.setVisibility(View.GONE);
                        dialog.dismiss();
                        checkUserRoleAndNavigate();
                    })
                    .addOnFailureListener(e -> {
                        progressBarPhone.setVisibility(View.GONE);
                        btnVerifyCode.setEnabled(true);
                        handleAuthError(e);
                    });
        });

        dialog.show();
    }

    private void checkUserRoleAndNavigate() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                if (user != null) {
                    if (user.isBarber()) {
                        startActivity(new Intent(LoginActivity.this, BarberMainActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, CustomerMainActivity.class));
                    }
                    finish();
                } else {
                    // First time login via Google/Phone -> prompt role selection
                    showRoleSelectionDialog();
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                // First time social/phone login user has no Firestore profile yet
                showRoleSelectionDialog();
            }
        });
    }

    private void showRoleSelectionDialog() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_role, null);
        EditText etRoleName = dialogView.findViewById(R.id.etRoleName);
        RadioButton rbDialogBarber = dialogView.findViewById(R.id.rbDialogBarber);

        String defaultName = fUser.getDisplayName() != null ? fUser.getDisplayName() : "Kullanıcı";
        etRoleName.setText(defaultName);

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(dialogView)
                .setPositiveButton("Tamam", (dialog, which) -> {
                    String name = etRoleName.getText().toString().trim();
                    boolean isBarber = rbDialogBarber.isChecked();
                    String role = isBarber ? User.ROLE_BARBER : User.ROLE_CUSTOMER;
                    String email = fUser.getEmail() != null ? fUser.getEmail() : "";
                    String phone = fUser.getPhoneNumber() != null ? fUser.getPhoneNumber() : "";

                    User newUser = new User(fUser.getUid(), name.isEmpty() ? defaultName : name, email, phone, role);
                    if (isBarber) {
                        newUser.setShopName(name + " Berber Dükkanı");
                        newUser.setWorkingHours("09:00 - 20:00");
                    }

                    firebaseHelper.saveUser(newUser, new FirebaseHelper.OnActionListener() {
                        @Override
                        public void onSuccess() {
                            if (isBarber) {
                                startActivity(new Intent(LoginActivity.this, BarberMainActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, CustomerMainActivity.class));
                            }
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(LoginActivity.this, "Rol kaydedilemedi: " + error, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, CustomerMainActivity.class));
                            finish();
                        }
                    });
                })
                .show();
    }

    private void handleAuthError(Exception e) {
        String errorMessage = e.getMessage();
        if (e instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_OPERATION_NOT_ALLOWED".equalsIgnoreCase(errorCode) || "CONFIGURATION_NOT_FOUND".equalsIgnoreCase(errorCode)) {
                errorMessage = "Firebase Console'da bu giriş yöntemi AÇIK değil!";
            } else if ("ERROR_USER_NOT_FOUND".equalsIgnoreCase(errorCode)) {
                errorMessage = "Kullanıcı bulunamadı.";
            } else if ("ERROR_WRONG_PASSWORD".equalsIgnoreCase(errorCode)) {
                errorMessage = "Hatalı şifre girdiniz.";
            } else if ("ERROR_INVALID_EMAIL".equalsIgnoreCase(errorCode)) {
                errorMessage = "Geçersiz e-posta adresi formatı.";
            }
        }
        Toast.makeText(LoginActivity.this, "Giriş başarısız: " + errorMessage, Toast.LENGTH_LONG).show();
    }
}