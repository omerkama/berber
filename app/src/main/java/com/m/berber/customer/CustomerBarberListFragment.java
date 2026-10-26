package com.m.berber.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.m.berber.R;
import com.m.berber.adapters.BarberAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerBarberListFragment extends Fragment {

    private EditText etSearchBarber;
    private RecyclerView rvBarbers;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private BarberAdapter adapter;
    private List<User> allBarbers = new ArrayList<>();
    private FirebaseHelper firebaseHelper;
    private String currentCustomerGender = User.GENDER_MALE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_barber_list, container, false);

        etSearchBarber = view.findViewById(R.id.etSearchBarber);
        rvBarbers = view.findViewById(R.id.rvBarbers);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        firebaseHelper = FirebaseHelper.getInstance();

        rvBarbers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BarberAdapter(barber -> {
            Intent intent = new Intent(getContext(), BookAppointmentActivity.class);
            intent.putExtra("barber", barber);
            startActivity(intent);
        });
        rvBarbers.setAdapter(adapter);

        etSearchBarber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBarbers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUserAndBarbers();

        return view;
    }

    private void loadUserAndBarbers() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentCustomerGender = user.getGender();
                }
                loadBarbers();
            }

            @Override
            public void onFailure(String error) {
                loadBarbers();
            }
        });
    }

    private void loadBarbers() {
        firebaseHelper.fetchBarbersForCustomer(currentCustomerGender, new FirebaseHelper.OnBarbersFetchListener() {
            @Override
            public void onSuccess(List<User> barbers) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                allBarbers = barbers;
                if (barbers.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
                adapter.setBarbers(barbers);
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Salonlar yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterBarbers(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setBarbers(allBarbers);
            return;
        }
        String lowerQuery = query.toLowerCase().trim();
        List<User> filtered = new ArrayList<>();
        for (User b : allBarbers) {
            String shopName = b.getShopName() != null ? b.getShopName().toLowerCase() : "";
            String name = b.getName() != null ? b.getName().toLowerCase() : "";
            String address = b.getShopAddress() != null ? b.getShopAddress().toLowerCase() : "";
            if (shopName.contains(lowerQuery) || name.contains(lowerQuery) || address.contains(lowerQuery)) {
                filtered.add(b);
            }
        }
        adapter.setBarbers(filtered);
    }
}