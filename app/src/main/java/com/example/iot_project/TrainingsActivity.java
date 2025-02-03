package com.example.iot_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iot_project.classes.DateTimePickerFragment;
import com.example.iot_project.classes.ItemTouchHelperCallback;
import com.example.iot_project.classes.ScheduledTraining;
import com.example.iot_project.classes.SpaceItemDecoration;
import com.example.iot_project.classes.Training;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.interfaces.ItemTouchHelperAdapter;
import com.example.iot_project.interfaces.TrainingsRecyclerViewInterface;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TrainingsActivity extends AppCompatActivity
        implements DateTimePickerFragment.OnDateTimeSetListener, TrainingsRecyclerViewInterface {

    private TrainingsActivity.TrainingsAdapter adapter;
    TrainingsViewModel trainingsViewModel;
    private Integer selectedPosition = 0;
    private Integer scheduledPosition = 0;
    private boolean switchTraining = false;
    private boolean isScheduled = false;
    private ImageView editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainings);

        Intent intent = getIntent();
        switchTraining = intent.getBooleanExtra("switchTraining", false);
        isScheduled = intent.getBooleanExtra("scheduled", false);
        scheduledPosition = intent.getIntExtra("position", 0);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_trainings_activity);
        // initialise your ViewModel here
        trainingsViewModel = new ViewModelProvider(this).get(TrainingsViewModel.class);
        adapter = new TrainingsAdapter(trainingsViewModel.getTrainings().getValue(), this, isScheduled);
        recyclerView.setAdapter(adapter);
        // Register an observer
        trainingsViewModel.getTrainings().observe(this, trainings -> {
            adapter.setTrainings(trainings);
        });

        // Sets replacing and removing handler
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // items decoration
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        // dynamic background
        AnimationDrawable animationDrawable = (AnimationDrawable) recyclerView.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }

    @Override
    public void onItemClick(int position) {
        selectedPosition = position;
        if (!switchTraining) {
            DialogFragment newFragment = new DateTimePickerFragment();
            newFragment.show(getSupportFragmentManager(), "dateTimePicker");
        }
        else {
            Training training = adapter.getItem(selectedPosition);
            Intent intent = new Intent();
            intent.putExtra("training", training);
            intent.putExtra("position", scheduledPosition);
            intent.putExtra("switchTraining", true);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onDateTimeSet(int year, int month, int day, int hour, int minute) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        Date dateTime = calendar.getTime();

        Training training = adapter.getItem(selectedPosition);
        ScheduledTraining scheduledTraining = new ScheduledTraining(training, dateTime, getBaseContext());
        scheduledTraining.save();
        Intent intent = new Intent();
        intent.putExtra("scheduledTraining", scheduledTraining);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private static class TrainingsAdapter extends RecyclerView.Adapter<TrainingsActivity.TrainingsViewHolder>
            implements ItemTouchHelperAdapter {
        private final TrainingsRecyclerViewInterface recyclerViewInterface;
        private List<Training> trainings;
        private boolean isScheduled;

        protected TrainingsAdapter(List<Training> trainings,
                                   TrainingsRecyclerViewInterface recyclerViewInterface, boolean isScheduled) {
            this.trainings = trainings;
            this.recyclerViewInterface = recyclerViewInterface;
            this.isScheduled = isScheduled;
        }

        @NonNull
        @Override
        public TrainingsActivity.TrainingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainings, parent, false);
            return new TrainingsViewHolder(itemView, recyclerViewInterface, isScheduled);
        }

        @Override
        public void onBindViewHolder(@NonNull TrainingsActivity.TrainingsViewHolder holder, int position) {
            holder.nameView.setText(trainings.get(position).getName());
            holder.imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(holder.imageView.getResources(),
                            trainings.get(position).getImage(),
                            null));

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
        }

        @Override
        public int getItemCount() {
            return (trainings != null ? trainings.size() : 0);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setTrainings(List<Training> trainings) {
            this.trainings = trainings;
            notifyDataSetChanged();
        }

        public Training getItem(int position) {
            return trainings.get(position);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            Collections.swap(trainings, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            Training training = trainings.get(position);
            Utilities.deleteFile(training.getPath(), training.getId());
            trainings.remove(position);
            notifyItemRemoved(position);
        }
    }

    private static class TrainingsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;

        public TrainingsViewHolder(View itemView, TrainingsRecyclerViewInterface recyclerViewInterface, boolean isScheduled) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_item_trainings);
            nameView = itemView.findViewById(R.id.text_view_item_trainings);
            if (isScheduled) {
                ImageView editImageView = itemView.findViewById(R.id.editTrainingImageView);
                editImageView.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getBindingAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}