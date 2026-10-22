package com.m.berber.customer;

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

import com.m.berber.R;
import com.m.berber.adapters.AppointmentAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.Appointment;
import com.m.berber.utils.NotificationHelper;

import java.util.List;

public class CustomerAppointmentsFragment extends Fragment {

    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private AppointmentAdapter adapter;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_appointments, container, false);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        firebaseHelper = FirebaseHelper.getInstance();

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentAdapter(false, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onApprove(Appointment appointment) {}

            @Override
            public void onReject(Appointment appointment) {}

            @Override
            public void onComplete(Appointment appointment) {}

            @Override
            public void onCancel(Appointment appointment) {
                cancelAppointment(appointment);
            }
        });
        rvAppointments.setAdapter(adapter);

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        String uid = firebaseHelper.getCurrentUid();
        if (uid == null) return;

        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.fetchCustomerAppointments(uid, new FirebaseHelper.OnAppointmentsFetchListener() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                if (appointments.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
                adapter.setAppointments(appointments);
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Randevular yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelAppointment(Appointment appointment) {
        firebaseHelper.updateAppointmentStatus(appointment.getId(), Appointment.STATUS_CANCELLED, new FirebaseHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Randevunuz iptal edildi.", Toast.LENGTH_SHORT).show();
                NotificationHelper.showNotification(getContext(), "Randevu İptal Edildi", "Randevunuz başarıyla iptal edildi.");
                loadAppointments();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "İptal edilemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}