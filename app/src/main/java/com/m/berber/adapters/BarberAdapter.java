package com.m.berber.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.m.berber.R;
import com.m.berber.models.User;

import java.util.ArrayList;
import java.util.List;

public class BarberAdapter extends RecyclerView.Adapter<BarberAdapter.BarberViewHolder> {

    private List<User> barberList = new ArrayList<>();
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(User barber);
    }

    public BarberAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBarbers(List<User> barbers) {
        this.barberList = barbers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barber, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        User barber = barberList.get(position);
        holder.tvShopName.setText(barber.getShopName() != null ? barber.getShopName() : barber.getName() + " Berber");
        holder.tvBarberName.setText("Berber: " + barber.getName());
        holder.tvShopAddress.setText("Adres: " + (barber.getShopAddress() != null ? barber.getShopAddress() : "Belirtilmemiş"));
        holder.tvWorkingHours.setText("Saatler: " + (barber.getWorkingHours() != null ? barber.getWorkingHours() : "09:00 - 20:00"));

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(barber);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName, tvBarberName, tvShopAddress, tvWorkingHours;
        MaterialButton btnBook;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvBarberName = itemView.findViewById(R.id.tvBarberName);
            tvShopAddress = itemView.findViewById(R.id.tvShopAddress);
            tvWorkingHours = itemView.findViewById(R.id.tvWorkingHours);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}