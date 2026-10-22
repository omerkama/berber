package com.m.berber.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.m.berber.R;
import com.m.berber.models.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private List<TimeSlot> slotList = new ArrayList<>();
    private int selectedPosition = -1;
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot slot, int position);
    }

    public SlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<TimeSlot> slots) {
        this.slotList = slots;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public TimeSlot getSelectedSlot() {
        if (selectedPosition >= 0 && selectedPosition < slotList.size()) {
            return slotList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        TimeSlot slot = slotList.get(position);
        Context context = holder.itemView.getContext();
        holder.tvSlotTime.setText(slot.getTime());

        if (position == selectedPosition) {
            holder.cardSlot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.accent));
            holder.tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardSlot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_bg));
            holder.tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int prevPos = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(prevPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onSlotClick(slot, selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSlot;
        TextView tvSlotTime;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
        }
    }
}