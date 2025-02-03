package com.example.iot_project.classes;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.R;
import com.example.iot_project.interfaces.ItemTouchHelperAdapter;
import com.example.iot_project.interfaces.NewTrainingRecyclerViewInterface;

import java.util.Collections;
import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.SetViewHolder> implements ItemTouchHelperAdapter {

    private final List<Set> setList;
    private final NewTrainingRecyclerViewInterface listener;

    public SetAdapter(List<Set> setList, NewTrainingRecyclerViewInterface listener) {
        this.setList = setList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Set set = setList.get(position);
        holder.bind(set);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ContentResolver contentResolver = v.getContext().getContentResolver();
                boolean hapticEnabled = Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0;

                if (hapticEnabled) {
                    // If haptic feedback is enabled, perform vibration
                    Vibrator vibrator = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // For newer APIs (26+)
                        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
                        vibrator.vibrate(vibrationEffect);
                    } else {
                        // Deprecated in API 26, but needed for older devices
                        vibrator.vibrate(100);
                    }
                }
                // Return true to indicate the long click was handled
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) { // check if item still exists
                    listener.onItemClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(setList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        setList.remove(position);
        notifyItemRemoved(position);
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewItemSet;
        private final TextView textViewItemSet;
        private final TextView textViewSetCyclesNum;
        private final TextView textViewSetRepetitionsNum;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItemSet = itemView.findViewById(R.id.image_view_item_set);
            textViewItemSet = itemView.findViewById(R.id.text_view_item_set);
            textViewSetCyclesNum = itemView.findViewById(R.id.text_view_set_cycles_num);
            textViewSetRepetitionsNum = itemView.findViewById(R.id.text_view_set_repetitions_num);
        }

        public void bind(Set set) {
            // Set the values for the views
            imageViewItemSet.setImageResource(set.getExercise().getImage());
            textViewItemSet.setText(set.getExercise().getName());
            textViewSetCyclesNum.setText(String.valueOf(set.getCycles()));
            textViewSetRepetitionsNum.setText(String.valueOf(set.getRepetitions()));
        }
    }
}
