package com.m.berber.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.m.berber.models.Appointment;
import com.m.berber.models.BarberShop;
import com.m.berber.models.ServiceItem;
import com.m.berber.models.TimeSlot;
import com.m.berber.models.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnBarbersFetchListener {
        void onSuccess(List<User> barbers);
        void onFailure(String error);
    }

    public interface OnAppointmentsFetchListener {
        void onSuccess(List<Appointment> appointments);
        void onFailure(String error);
    }

    public interface OnActionListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnSlotsFetchListener {
        void onSuccess(List<TimeSlot> slots);
        void onFailure(String error);
    }

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // Save or update user profile in Firestore
    public void saveUser(User user, OnActionListener listener) {
        if (user.getUid() == null) {
            listener.onFailure("Kullanıcı ID'si geçersiz.");
            return;
        }
        db.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch current user details
    public void fetchCurrentUser(OnUserFetchListener listener) {
        String uid = getCurrentUid();
        if (uid == null) {
            listener.onFailure("Oturum açılmamış.");
            return;
        }
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("Kullanıcı profili bulunamadı.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch all barbers for Customer list
    public void fetchBarbers(OnBarbersFetchListener listener) {
        db.collection("users")
                .whereEqualTo("role", User.ROLE_BARBER)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> barbers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User barber = doc.toObject(User.class);
                        barbers.add(barber);
                    }
                    listener.onSuccess(barbers);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Save or Update Barber Services List
    public void saveBarberServices(String barberId, List<ServiceItem> services, OnActionListener listener) {
        db.collection("barber_services").document(barberId)
                .set(new BarberShopServicesWrapper(services))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch Barber Services
    public void fetchBarberServices(String barberId, OnServicesFetchListener listener) {
        db.collection("barber_services").document(barberId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        BarberShopServicesWrapper wrapper = doc.toObject(BarberShopServicesWrapper.class);
                        if (wrapper != null && wrapper.getServices() != null) {
                            listener.onSuccess(wrapper.getServices());
                            return;
                        }
                    }
                    listener.onSuccess(new ArrayList<>());
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnServicesFetchListener {
        void onSuccess(List<ServiceItem> services);
        void onFailure(String error);
    }

    public static class BarberShopServicesWrapper {
        private List<ServiceItem> services;

        public BarberShopServicesWrapper() {}

        public BarberShopServicesWrapper(List<ServiceItem> services) {
            this.services = services;
        }

        public List<ServiceItem> getServices() {
            return services;
        }

        public void setServices(List<ServiceItem> services) {
            this.services = services;
        }
    }

    // Add Time Slot for Barber
    public void addTimeSlot(TimeSlot slot, OnActionListener listener) {
        String docId = db.collection("slots").document().getId();
        slot.setId(docId);
        db.collection("slots").document(docId)
                .set(slot)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch available time slots for barber on date
    public void fetchSlotsForBarber(String barberId, String date, OnSlotsFetchListener listener) {
        db.collection("slots")
                .whereEqualTo("barberId", barberId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(query -> {
                    List<TimeSlot> slots = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        TimeSlot slot = doc.toObject(TimeSlot.class);
                        if (slot != null) {
                            slots.add(slot);
                        }
                    }
                    listener.onSuccess(slots);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Book an appointment
    public void createAppointment(Appointment appointment, OnActionListener listener) {
        String docId = db.collection("appointments").document().getId();
        appointment.setId(docId);
        appointment.setCreatedAt(System.currentTimeMillis());
        db.collection("appointments").document(docId)
                .set(appointment)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch appointments for customer
    public void fetchCustomerAppointments(String customerId, OnAppointmentsFetchListener listener) {
        db.collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Appointment> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Appointment app = doc.toObject(Appointment.class);
                        if (app != null) list.add(app);
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Fetch appointments for barber
    public void fetchBarberAppointments(String barberId, OnAppointmentsFetchListener listener) {
        db.collection("appointments")
                .whereEqualTo("barberId", barberId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Appointment> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Appointment app = doc.toObject(Appointment.class);
                        if (app != null) list.add(app);
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Update appointment status (BEKLEMEDE, ONAYLANDI, REDDEDILDI, TAMAMLANDI, IPTAL)
    public void updateAppointmentStatus(String appointmentId, String newStatus, OnActionListener listener) {
        db.collection("appointments").document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}