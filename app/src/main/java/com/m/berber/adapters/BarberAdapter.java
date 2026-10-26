package com.m.berber.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.m.berber.R;
import com.m.berber.chat.ChatActivity;
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
        Context context = holder.itemView.getContext();

        holder.tvShopName.setText(barber.getShopName() != null ? barber.getShopName() : barber.getName() + " Berber");
        holder.tvBarberName.setText("Berber: " + barber.getName());
        holder.tvShopAddress.setText("Adres: " + (barber.getShopAddress() != null ? barber.getShopAddress() : "Belirtilmemiş"));
        holder.tvWorkingHours.setText("Saatler: " + (barber.getWorkingHours() != null ? barber.getWorkingHours() : "09:00 - 20:00"));

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(barber);
            }
        });

        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("recipientUid", barber.getUid());
            intent.putExtra("recipientName", barber.getShopName() != null ? barber.getShopName() : barber.getName());
            intent.putExtra("recipientPhone", barber.getPhone());
            context.startActivity(intent);
        });

        holder.btnCall.setOnClickListener(v -> {
            if (barber.getPhone() != null && !barber.getPhone().trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + barber.getPhone().trim()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Telefon numarası bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    static class BarberViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName, tvBarberName, tvShopAddress, tvWorkingHours;
        MaterialButton btnBook, btnChat, btnCall;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvBarberName = itemView.findViewById(R.id.tvBarberName);
            tvShopAddress = itemView.findViewById(R.id.tvShopAddress);
            tvWorkingHours = itemView.findViewById(R.id.tvWorkingHours);
            btnBook = itemView.findViewById(R.id.btnBook);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}