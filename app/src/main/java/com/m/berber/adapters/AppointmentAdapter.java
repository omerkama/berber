package com.m.berber.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.m.berber.R;
import com.m.berber.models.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList = new ArrayList<>();
    private final boolean isBarberView;
    private final OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onApprove(Appointment appointment);
        void onReject(Appointment appointment);
        void onComplete(Appointment appointment);
        void onCancel(Appointment appointment);
    }

    public AppointmentAdapter(boolean isBarberView, OnAppointmentActionListener listener) {
        this.isBarberView = isBarberView;
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        Context context = holder.itemView.getContext();

        if (isBarberView) {
            holder.tvTitleName.setText(appointment.getCustomerName());
            holder.tvSubtitle.setText("Tel: " + appointment.getCustomerPhone());
            holder.layoutBarberActions.setVisibility(View.VISIBLE);
            holder.btnCancelCustomer.setVisibility(View.GONE);

            // Button visibility based on status
            if (Appointment.STATUS_PENDING.equalsIgnoreCase(appointment.getStatus())) {
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnComplete.setVisibility(View.GONE);
            } else if (Appointment.STATUS_APPROVED.equalsIgnoreCase(appointment.getStatus())) {
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnComplete.setVisibility(View.VISIBLE);
            } else {
                holder.layoutBarberActions.setVisibility(View.GONE);
            }
        } else {
            holder.tvTitleName.setText(appointment.getShopName() != null ? appointment.getShopName() : appointment.getBarberName());
            holder.tvSubtitle.setText("Berber: " + appointment.getBarberName());
            holder.layoutBarberActions.setVisibility(View.GONE);

            if (Appointment.STATUS_PENDING.equalsIgnoreCase(appointment.getStatus()) ||
                Appointment.STATUS_APPROVED.equalsIgnoreCase(appointment.getStatus())) {
                holder.btnCancelCustomer.setVisibility(View.VISIBLE);
            } else {
                holder.btnCancelCustomer.setVisibility(View.GONE);
            }
        }

        holder.tvServiceAndPrice.setText("Hizmet: " + appointment.getServiceName() + " (" + appointment.getPrice() + " TL)");
        holder.tvDate.setText(appointment.getDate());
        holder.tvTime.setText(appointment.getTime());

        // Status Badge formatting
        String status = appointment.getStatus();
        holder.tvStatusBadge.setText(status != null ? status : "BEKLEMEDE");
        if (Appointment.STATUS_APPROVED.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_approved));
        } else if (Appointment.STATUS_REJECTED.equalsIgnoreCase(status) || Appointment.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
        } else if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
        } else {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
        }

        // Listeners
        holder.btnApprove.setOnClickListener(v -> { if (listener != null) listener.onApprove(appointment); });
        holder.btnReject.setOnClickListener(v -> { if (listener != null) listener.onReject(appointment); });
        holder.btnComplete.setOnClickListener(v -> { if (listener != null) listener.onComplete(appointment); });
        holder.btnCancelCustomer.setOnClickListener(v -> { if (listener != null) listener.onCancel(appointment); });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitleName, tvSubtitle, tvStatusBadge, tvServiceAndPrice, tvDate, tvTime;
        LinearLayout layoutBarberActions;
        MaterialButton btnApprove, btnReject, btnComplete, btnCancelCustomer;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitleName = itemView.findViewById(R.id.tvTitleName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvServiceAndPrice = itemView.findViewById(R.id.tvServiceAndPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            layoutBarberActions = itemView.findViewById(R.id.layoutBarberActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnCancelCustomer = itemView.findViewById(R.id.btnCancelCustomer);
        }
    }
}