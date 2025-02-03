package com.example.iot_project.classes;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private List<Integer> iconList;
    //private List<String> iconDescriptions;
    private IconClickListener iconClickListener;

    public interface IconClickListener {
        void onIconClick(int iconId);
    }

    public IconAdapter(List<Integer> iconList, IconClickListener iconClickListener) {
        this.iconList = iconList;
        //this.iconDescriptions = iconDescriptions;
        this.iconClickListener = iconClickListener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_grid_icon, parent, false);
        return new IconViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        int iconId = iconList.get(position);
        holder.iconImageView.setImageResource(iconId);
        holder.itemView.setOnClickListener(v -> {
            if (iconClickListener != null) {
                iconClickListener.onIconClick(iconId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconImageView;

        public IconViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
        }
    }
}
