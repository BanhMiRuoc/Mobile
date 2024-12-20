package com.example.ex4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private int itemCount;
    private boolean[] itemStates; // Stores ON/OFF states of each item
    private Context context;

    public GridAdapter(int itemCount, Context context) {
        this.itemCount = itemCount;
        this.context = context;
        itemStates = new boolean[itemCount]; // Initialize all states to OFF (false)
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set the text to PC number
        holder.textView.setText("PC " + (position + 1));

        // Set the icon based on the item's state (ON/OFF)
        holder.imageView.setImageResource(itemStates[position] ? R.drawable.icon_on : R.drawable.icon_off);

        // Handle click event to toggle the state
        holder.itemView.setOnClickListener(v -> {
            // Toggle the state
            itemStates[position] = !itemStates[position];
            // Notify the adapter to refresh this item
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_icon);
            textView = itemView.findViewById(R.id.item_text);
        }
    }
}

