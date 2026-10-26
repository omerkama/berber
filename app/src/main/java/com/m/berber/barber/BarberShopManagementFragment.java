package com.m.berber.barber;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.m.berber.R;
import com.m.berber.adapters.ServiceAdapter;
import com.m.berber.adapters.SlotAdapter;
import com.m.berber.auth.FirebaseHelper;
import com.m.berber.models.ServiceItem;
import com.m.berber.models.TimeSlot;
import com.m.berber.models.User;
import com.m.berber.utils.PrayerTimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BarberShopManagementFragment extends Fragment {

    private TextInputEditText etShopName, etShopAddress, etWorkingHours;
    private MaterialButton btnSaveShopInfo, btnAddService, btnAddSlot;
    private RecyclerView rvServices, rvSlots;

    private ServiceAdapter serviceAdapter;
    private SlotAdapter slotAdapter;
    private List<ServiceItem> currentServices = new ArrayList<>();

    private FirebaseHelper firebaseHelper;
    private User currentBarber;
    private String slotSelectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barber_shop_management, container, false);

        etShopName = view.findViewById(R.id.etShopName);
        etShopAddress = view.findViewById(R.id.etShopAddress);
        etWorkingHours = view.findViewById(R.id.etWorkingHours);
        btnSaveShopInfo = view.findViewById(R.id.btnSaveShopInfo);
        btnAddService = view.findViewById(R.id.btnAddService);
        btnAddSlot = view.findViewById(R.id.btnAddSlot);
        rvServices = view.findViewById(R.id.rvServices);
        rvSlots = view.findViewById(R.id.rvSlots);

        firebaseHelper = FirebaseHelper.getInstance();

        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        serviceAdapter = new ServiceAdapter(true, position -> {
            if (position >= 0 && position < currentServices.size()) {
                currentServices.remove(position);
                serviceAdapter.setServices(currentServices);
                saveServicesToFirebase();
            }
        });
        rvServices.setAdapter(serviceAdapter);

        rvSlots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        slotAdapter = new SlotAdapter((slot, position) -> {});
        rvSlots.setAdapter(slotAdapter);

        btnSaveShopInfo.setOnClickListener(v -> saveShopInfo());
        btnAddService.setOnClickListener(v -> showAddServiceDialog());
        btnAddSlot.setOnClickListener(v -> showAddSlotDialog());

        loadBarberInfo();

        return view;
    }

    private void loadBarberInfo() {
        firebaseHelper.fetchCurrentUser(new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (getContext() == null || user == null) return;
                currentBarber = user;
                etShopName.setText(user.getShopName() != null ? user.getShopName() : "");
                etShopAddress.setText(user.getShopAddress() != null ? user.getShopAddress() : "");
                etWorkingHours.setText(user.getWorkingHours() != null ? user.getWorkingHours() : "09:00 - 20:00");

                loadServices();
                setDefaultTodaySlots();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Bilgiler yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveShopInfo() {
        if (currentBarber == null) return;

        String shopName = etShopName.getText() != null ? etShopName.getText().toString().trim() : "";
        String shopAddress = etShopAddress.getText() != null ? etShopAddress.getText().toString().trim() : "";
        String workingHours = etWorkingHours.getText() != null ? etWorkingHours.getText().toString().trim() : "";

        if (TextUtils.isEmpty(shopName)) {
            etShopName.setError("Dükkan adı gereklidir");
            return;
        }

        currentBarber.setShopName(shopName);
        currentBarber.setShopAddress(shopAddress);
        currentBarber.setWorkingHours(workingHours);

        firebaseHelper.saveUser(currentBarber, new FirebaseHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Dükkan bilgileri güncellendi!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Güncelleme hatası: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices() {
        if (currentBarber == null) return;
        firebaseHelper.fetchBarberServices(currentBarber.getUid(), new FirebaseHelper.OnServicesFetchListener() {
            @Override
            public void onSuccess(List<ServiceItem> services) {
                if (getContext() == null) return;
                currentServices = services;
                serviceAdapter.setServices(currentServices);
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Hizmetler yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveServicesToFirebase() {
        if (currentBarber == null) return;
        firebaseHelper.saveBarberServices(currentBarber.getUid(), currentServices, new FirebaseHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Hizmetler kaydedildi.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Hizmet kaydetme hatası: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddServiceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_service, null);
        EditText etServiceName = dialogView.findViewById(R.id.etServiceName);
        EditText etServicePrice = dialogView.findViewById(R.id.etServicePrice);
        EditText etServiceDuration = dialogView.findViewById(R.id.etServiceDuration);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Ekle", (dialog, which) -> {
                    String name = etServiceName.getText().toString().trim();
                    String priceStr = etServicePrice.getText().toString().trim();
                    String durationStr = etServiceDuration.getText().toString().trim();

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(priceStr)) {
                        double price = Double.parseDouble(priceStr);
                        int duration = TextUtils.isEmpty(durationStr) ? 30 : Integer.parseInt(durationStr);
                        String serviceId = "srv_" + System.currentTimeMillis();

                        currentServices.add(new ServiceItem(serviceId, name, price, duration));
                        serviceAdapter.setServices(currentServices);
                        saveServicesToFirebase();
                    } else {
                        Toast.makeText(getContext(), "Lütfen tüm bilgileri doldurun.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void setDefaultTodaySlots() {
        Calendar c = Calendar.getInstance();
        slotSelectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        loadSlotsForDate(slotSelectedDate);
    }

    private void loadSlotsForDate(String date) {
        if (currentBarber == null) return;
        firebaseHelper.fetchSlotsForBarber(currentBarber.getUid(), date, new FirebaseHelper.OnSlotsFetchListener() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                if (getContext() == null) return;
                slotAdapter.setSlots(slots);
            }

            @Override
            public void onFailure(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Saatler yüklenemedi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSlotDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_slot, null);
        MaterialButton btnSlotDate = dialogView.findViewById(R.id.btnSlotDate);
        EditText etSlotTime = dialogView.findViewById(R.id.etSlotTime);

        final String[] tempDate = {slotSelectedDate};
        btnSlotDate.setText("Tarih: " + tempDate[0]);

        btnSlotDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                tempDate[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnSlotDate.setText("Tarih: " + tempDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Ekle", (dialog, which) -> {
                    String time = etSlotTime.getText().toString().trim();
                    if (!TextUtils.isEmpty(time)) {

                        // Check Prayer Time and Friday Restrictions
                        String prayerReason = PrayerTimeUtil.getPrayerRestrictionReason(tempDate[0], time);
                        if (prayerReason != null) {
                            Toast.makeText(getContext(), prayerReason + "\nNamaz ve Cuma vakitlerinde randevu saati açılamaz!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        TimeSlot slot = new TimeSlot(null, currentBarber.getUid(), tempDate[0], time, true);
                        firebaseHelper.addTimeSlot(slot, new FirebaseHelper.OnActionListener() {
                            @Override
                            public void onSuccess() {
                                if (getContext() == null) return;
                                Toast.makeText(getContext(), "Saat dilimi eklendi!", Toast.LENGTH_SHORT).show();
                                slotSelectedDate = tempDate[0];
                                loadSlotsForDate(slotSelectedDate);
                            }

                            @Override
                            public void onFailure(String error) {
                                if (getContext() == null) return;
                                Toast.makeText(getContext(), "Saat eklenemedi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }
}