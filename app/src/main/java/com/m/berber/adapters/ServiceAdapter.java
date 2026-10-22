package com.m.berber.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.m.berber.R;
import com.m.berber.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceItem> serviceList = new ArrayList<>();
    private final boolean isEditable;
    private final OnServiceDeleteListener deleteListener;

    public interface OnServiceDeleteListener {
        void onDeleteService(int position);
    }

    public ServiceAdapter(boolean isEditable, OnServiceDeleteListener deleteListener) {
        this.isEditable = isEditable;
        this.deleteListener = deleteListener;
    }

    public void setServices(List<ServiceItem> services) {
        this.serviceList = services;
        notifyDataSetChanged();
    }

    public List<ServiceItem> getServices() {
        return serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem item = serviceList.get(position);
        holder.tvServiceName.setText(item.getName());
        holder.tvServicePrice.setText(item.getPrice() + " TL");
        holder.tvServiceDuration.setText("Süre: " + item.getDurationMinutes() + " dk");

        if (isEditable) {
            holder.btnDeleteService.setVisibility(View.VISIBLE);
            holder.btnDeleteService.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteService(holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnDeleteService.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServicePrice, tvServiceDuration;
        ImageButton btnDeleteService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceDuration = itemView.findViewById(R.id.tvServiceDuration);
            btnDeleteService = itemView.findViewById(R.id.btnDeleteService);
        }
    }
}