package com.example.iot_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.classes.ScheduledTraining;
import com.example.iot_project.classes.Training;
import com.example.iot_project.classes.ItemTouchHelperCallback;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.databinding.FragmentTrainingsBinding;
import com.example.iot_project.databinding.ItemTrainingsBinding;
import com.example.iot_project.interfaces.TrainingsRecyclerViewInterface;
import com.example.iot_project.interfaces.ItemTouchHelperAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrainingsFragment extends Fragment implements TrainingsRecyclerViewInterface {

    private FragmentTrainingsBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private Bundle savedState = new Bundle();
    private TrainingsAdapter adapter;
    private TrainingsViewModel trainingsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                savedState = data.getBundleExtra("savedInstanceState");
                            }
                            else if (result.getResultCode() == Activity.RESULT_OK) {
                                Training training =
                                        (Training) data.getSerializableExtra("training");
                                if (data.getBooleanExtra("new", false)) {
                                    trainingsViewModel.addTraining(training);
                                    adapter.notifyItemInserted(adapter.getItemCount());
                                }
                                else {
                                    int position = data.getIntExtra("position", 0);
                                    Objects.requireNonNull(trainingsViewModel.getTrainings().
                                            getValue()).set(position, training);
                                    adapter.notifyItemChanged(position);
                                }

                            }
                        }
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        trainingsViewModel = new ViewModelProvider(this).get(TrainingsViewModel.class);

        binding = FragmentTrainingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewTrainingsFragment;
        adapter = new TrainingsAdapter(trainingsViewModel.getTrainings().getValue(),
                this, launcher, getContext());
        recyclerView.setAdapter(adapter);
        // Register an observer
        trainingsViewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            adapter.setTrainings(trainings);
        });

        // Sets replacing and removing handler
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // set new_item button
        FloatingActionButton newItemButton = requireActivity().findViewById(R.id.new_item_button);
        newItemButton.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {
        Training training =
                Objects.requireNonNull(trainingsViewModel.
                        getTrainings().getValue()).get(position);
        Intent intent = new Intent(getContext(), RepetitionsCounterActivity.class);
        intent.putExtra("training", training);
        intent.putExtra("position", position);
        intent.putExtra("scheduled", false);
        launcher.launch(intent);
    }

    private static class TrainingsAdapter extends RecyclerView.Adapter<TrainingsViewHolder>
            implements ItemTouchHelperAdapter {
        private final TrainingsRecyclerViewInterface recyclerViewInterface;
        private List<Training> trainings;
        private final ActivityResultLauncher<Intent> launcher;
        private final Context context;
        private int selectedPosition;

        protected TrainingsAdapter(List<Training> trainings,
                                   TrainingsRecyclerViewInterface recyclerViewInterface,
                                   ActivityResultLauncher<Intent> launcher, Context context) {
            this.trainings = trainings;
            this.recyclerViewInterface = recyclerViewInterface;
            this.launcher = launcher;
            this.context = context;
        }

        @NonNull
        @Override
        public TrainingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemTrainingsBinding binding = ItemTrainingsBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new TrainingsFragment.TrainingsViewHolder(binding, recyclerViewInterface);
        }

        @Override
        public void onBindViewHolder(@NonNull TrainingsFragment.TrainingsViewHolder holder, int position) {
            holder.nameView.setText(trainings.get(position).getName());
            holder.imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(holder.imageView.getResources(),
                            trainings.get(position).getImage(),
                            null));

            assert holder.editTrainingImageView != null;
            holder.editTrainingImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = holder.getBindingAdapterPosition();
                    Training training = trainings.get(selectedPosition);
                    Intent intent = new Intent(context, TrainingActivity.class);
                    intent.putExtra("training", training);
                    intent.putExtra("isModification", true);
                    intent.putExtra("position", selectedPosition);
                    launcher.launch(intent);
                }
            });

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
        private final ImageView editTrainingImageView;

        public TrainingsViewHolder(ItemTrainingsBinding binding,
                                   TrainingsRecyclerViewInterface recyclerViewInterface) {
            super(binding.getRoot());
            imageView = binding.imageViewItemTrainings;
            nameView = binding.textViewItemTrainings;
            editTrainingImageView = binding.editTrainingImageView;


            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getBindingAdapterPosition();

                        if (position!= RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }

                }
            });
        }
    }

    public void onNewItemButtonCLick() {
        Intent intent = new Intent(getContext(), TrainingActivity.class);
        intent.putExtra("savedInstanceState", savedState);
        launcher.launch(intent);
    }
}