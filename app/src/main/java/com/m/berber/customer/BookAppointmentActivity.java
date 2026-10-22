package com.m.berber.customer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.m.berber.R;
import com.m.berber.adapters.SlotAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.Appointment;
import com.m.berber.models.ServiceItem;
import com.m.berber.models.TimeSlot;
import com.m.berber.models.User;
import com.m.berber.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private TextView tvShopName, tvShopDetails, tvNoSlots;
    private Spinner spinnerServices;
    private MaterialButton btnSelectDate, btnConfirmBooking;
    private RecyclerView rvSlots;
    private ProgressBar progressBar;

    private User targetBarber;
    private User currentUser;
    private SlotAdapter slotAdapter;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private String selectedDate = "";
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        targetBarber = (User) getIntent().getSerializableExtra("barber");
        if (targetBarber == null) {
            Toast.makeText(this, "Berber bilgisi bulunamadı.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper = FirebaseHelper.getInstance();

        tvShopName = findViewById(R.id.tvShopName);
        tvShopDetails = findViewById(R.id.tvShopDetails);
        tvNoSlots = findViewById(R.id.tvNoSlots);
        spinnerServices = findViewById(R.id.spinnerServices);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        rvSlots = findViewById(R.id.rvSlots);
        progressBar = findViewById(R.id.progressBar);

        tvShopName.setText(targetBarber.getShopName() != null ? targetBarber.getShopName() : targetBarber.getName());
        tvShopDetails.setText("Adres: " + (targetBarber.getShopAddress() != null ? targetBarber.getShopAddress() : "-") + "\nSaatler: " + (targetBarber.getWorkingHours() != null ? targetBarber.getWorkingHours() : "09:00 - 20:00"));

        rvSlots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        slotAdapter = new SlotAdapter((slot, position) -> {});
        rvSlots.setAdapter(slotAdapter);

        // Fetch current customer details
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
            }

            @Override
            public void onFailure(String error) {}
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        loadServices();
        setDefaultTodayDate();
    }

    private void setDefaultTodayDate() {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d);
        btnSelectDate.setText("Tarih: " + selectedDate);
        loadTimeSlots();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            btnSelectDate.setText("Tarih: " + selectedDate);
            loadTimeSlots();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadServices() {
        firebaseHelper.fetchBarberServices(targetBarber.getUid(), new FirebaseHelper.OnServicesFetchListener() {
            @Override
            public void onSuccess(List<ServiceItem> services) {
                serviceList = services;
                List<String> serviceNames = new ArrayList<>();
                if (services.isEmpty()) {
                    serviceNames.add("Standart Saç Kesimi (150 TL)");
                    // Default fallback service item
                    serviceList.add(new ServiceItem("def1", "Standart Saç Kesimi", 150, 30));
                } else {
                    for (ServiceItem s : services) {
                        serviceNames.add(s.getName() + " (" + s.getPrice() + " TL)");
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BookAppointmentActivity.this, android.R.layout.simple_spinner_item, serviceNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerServices.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookAppointmentActivity.this, "Hizmetler alınamadı: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTimeSlots() {
        if (selectedDate == null || selectedDate.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.fetchSlotsForBarber(targetBarber.getUid(), selectedDate, new FirebaseHelper.OnSlotsFetchListener() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                progressBar.setVisibility(View.GONE);
                if (slots.isEmpty()) {
                    // Generate default demo slots if none configured in Firestore
                    slots = createDefaultSlots();
                }
                tvNoSlots.setVisibility(slots.isEmpty() ? View.VISIBLE : View.GONE);
                slotAdapter.setSlots(slots);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                List<TimeSlot> defaultSlots = createDefaultSlots();
                slotAdapter.setSlots(defaultSlots);
            }
        });
    }

    private List<TimeSlot> createDefaultSlots() {
        List<TimeSlot> list = new ArrayList<>();
        String[] hours = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        for (String h : hours) {
            list.add(new TimeSlot("slot_" + h, targetBarber.getUid(), selectedDate, h, true));
        }
        return list;
    }

    private void confirmBooking() {
        TimeSlot selectedSlot = slotAdapter.getSelectedSlot();
        if (selectedSlot == null) {
            Toast.makeText(this, "Lütfen bir saat dilimi seçiniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        int servicePos = spinnerServices.getSelectedItemPosition();
        ServiceItem selectedService = (serviceList != null && servicePos >= 0 && servicePos < serviceList.size())
                ? serviceList.get(servicePos)
                : new ServiceItem("def", "Saç Kesimi", 150, 30);

        String customerId = firebaseHelper.getCurrentUid();
        String customerName = currentUser != null ? currentUser.getName() : "Müşteri";
        String customerPhone = currentUser != null ? currentUser.getPhone() : "-";

        Appointment app = new Appointment(
                null,
                targetBarber.getUid(),
                targetBarber.getName(),
                targetBarber.getShopName() != null ? targetBarber.getShopName() : targetBarber.getName(),
                customerId,
                customerName,
                customerPhone,
                selectedService.getName(),
                selectedService.getPrice(),
                selectedDate,
                selectedSlot.getTime(),
                Appointment.STATUS_PENDING,
                System.currentTimeMillis()
        );

        progressBar.setVisibility(View.VISIBLE);
        btnConfirmBooking.setEnabled(false);

        firebaseHelper.createAppointment(app, new FirebaseHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookAppointmentActivity.this, "Randevu talebiniz berbere iletildi!", Toast.LENGTH_LONG).show();
                NotificationHelper.showNotification(BookAppointmentActivity.this, "Randevu Başvurusu Oluşturuldu",
                        targetBarber.getName() + " için " + selectedDate + " " + selectedSlot.getTime() + " randevunuz alındı.");
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnConfirmBooking.setEnabled(true);
                Toast.makeText(BookAppointmentActivity.this, "Randevu oluşturulamadı: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}