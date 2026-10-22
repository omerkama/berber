package com.m.berber.barber;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.m.berber.R;
import com.m.berber.adapters.AppointmentAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.Appointment;
import com.m.berber.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class BarberAppointmentsFragment extends Fragment {

    private TabLayout tabLayoutStatus;
    private RecyclerView rvBarberAppointments;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private AppointmentAdapter adapter;
    private List<Appointment> allAppointments = new ArrayList<>();
    private FirebaseHelper firebaseHelper;
    private int selectedTabPosition = 0; // 0: Bekleyen, 1: Onaylanan, 2: Tümü

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barber_appointments, container, false);

        tabLayoutStatus = view.findViewById(R.id.tabLayoutStatus);
        rvBarberAppointments = view.findViewById(R.id.rvBarberAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        firebaseHelper = FirebaseHelper.getInstance();

        rvBarberAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentAdapter(true, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onApprove(Appointment appointment) {
                updateStatus(appointment, Appointment.STATUS_APPROVED, "Randevu Onaylandı", appointment.getCustomerName() + " için randevu onaylandı.");
            }

            @Override
            public void onReject(Appointment appointment) {
                updateStatus(appointment, Appointment.STATUS_REJECTED, "Randevu Reddedildi", appointment.getCustomerName() + " için randevu reddedildi.");
            }

            @Override
            public void onComplete(Appointment appointment) {
                updateStatus(appointment, Appointment.STATUS_COMPLETED, "Randevu Tamamlandı", appointment.getCustomerName() + " randevusu tamamlandı.");
            }

            @Override
            public void onCancel(Appointment appointment) {
                updateStatus(appointment, Appointment.STATUS_CANCELLED, "Randevu İptal Edildi", "Randevu iptal edildi.");
            }
        });
        rvBarberAppointments.setAdapter(adapter);

        tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabPosition = tab.getPosition();
                filterAppointments();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        String uid = firebaseHelper.getCurrentUid();
        if (uid == null) return;

        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.fetchBarberAppointments(uid, new FirebaseHelper.OnAppointmentsFetchListener() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                allAppointments = appointments;
                filterAppointments();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Randevular yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAppointments() {
        List<Appointment> filtered = new ArrayList<>();
        if (selectedTabPosition == 0) {
            // Bekleyen
            for (Appointment a : allAppointments) {
                if (Appointment.STATUS_PENDING.equalsIgnoreCase(a.getStatus())) {
                    filtered.add(a);
                }
            }
        } else if (selectedTabPosition == 1) {
            // Onaylanan
            for (Appointment a : allAppointments) {
                if (Appointment.STATUS_APPROVED.equalsIgnoreCase(a.getStatus())) {
                    filtered.add(a);
                }
            }
        } else {
            // Tümü
            filtered.addAll(allAppointments);
        }

        if (filtered.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.setAppointments(filtered);
    }

    private void updateStatus(Appointment appointment, String newStatus, String notifTitle, String notifBody) {
        firebaseHelper.updateAppointmentStatus(appointment.getId(), newStatus, new FirebaseHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Durum güncellendi: " + newStatus, Toast.LENGTH_SHORT).show();
                NotificationHelper.showNotification(getContext(), notifTitle, notifBody);
                loadAppointments();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Güncellenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}