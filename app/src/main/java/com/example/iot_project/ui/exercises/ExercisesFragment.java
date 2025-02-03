package com.example.iot_project.ui.exercises;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.example.iot_project.Constants;
import com.example.iot_project.ExerciseActivity;
import com.example.iot_project.R;
import com.example.iot_project.TrainingActivity;
import com.example.iot_project.classes.Exercise;
import com.example.iot_project.classes.ItemTouchHelperCallback;
import com.example.iot_project.classes.ScheduledTraining;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.databinding.FragmentExercisesBinding;
import com.example.iot_project.databinding.ItemExercisesBinding;
import com.example.iot_project.interfaces.ExercisesRecyclerViewInterface;
import com.example.iot_project.interfaces.ItemTouchHelperAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExercisesFragment extends Fragment implements ExercisesRecyclerViewInterface {

    private FragmentExercisesBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private Bundle savedState = new Bundle();
    private ExercisesAdapter adapter;
    private ExercisesViewModel exercisesViewModel;

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
                                Exercise exercise =
                                        (Exercise) data.getSerializableExtra("exercise");
                                if (data.getBooleanExtra("new", false)) {
                                    exercisesViewModel.addExercise(exercise);
                                    adapter.notifyItemInserted(adapter.getItemCount());
                                }
                                else {
                                    int position = data.getIntExtra("position", 0);
                                    Objects.requireNonNull(exercisesViewModel.getExercises().
                                            getValue()).set(position, exercise);
                                    adapter.notifyItemChanged(position);
                                }

                            }
                        }
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentExercisesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // set new_item button
        FloatingActionButton newItemButton = requireActivity().findViewById(R.id.new_item_button);
        newItemButton.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);

        RecyclerView recyclerView = binding.recyclerviewExercisesFragment;
        adapter = new ExercisesAdapter(exercisesViewModel.getExercises().getValue(), this);
        recyclerView.setAdapter(adapter);
        // Register an observer
        exercisesViewModel.getExercises().observe(getViewLifecycleOwner(), exercises -> {
            adapter.setExercises(exercises);
        });

        // Sets replacing and removing handler
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {
        Exercise exercise =
                Objects.requireNonNull(exercisesViewModel.
                        getExercises().getValue()).get(position);
        if (!exercise.isLocked()) {
            Intent intent = new Intent(getContext(), ExerciseActivity.class);
            intent.putExtra("exercise", exercise);
            intent.putExtra("isModification", true);
            intent.putExtra("position", position);
            intent.putExtra("fromFragment", true);
            launcher.launch(intent);
        }
        else {
            Toast.makeText(getContext(), "Built-in exercises are locked", Toast.LENGTH_SHORT).show();
        }

    }

    private static class ExercisesAdapter extends RecyclerView.Adapter<ExercisesViewHolder>
            implements ItemTouchHelperAdapter {
        private final ExercisesRecyclerViewInterface recyclerViewInterface;
        private List<Exercise> exercises;


        protected ExercisesAdapter(List<Exercise> exercises,
                                   ExercisesRecyclerViewInterface recyclerViewInterface) {
            this.exercises = exercises;
            this.recyclerViewInterface = recyclerViewInterface;
        }

        @NonNull
        @Override
        public ExercisesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemExercisesBinding binding = ItemExercisesBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new ExercisesFragment.ExercisesViewHolder(binding, recyclerViewInterface);
        }

        @Override
        public void onBindViewHolder(@NonNull ExercisesFragment.ExercisesViewHolder holder, int position) {
            holder.nameView.setText(exercises.get(position).getName());
            holder.imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(holder.imageView.getResources(),
                            exercises.get(position).getImage(),
                            null));
            if (exercises.get(position).isLocked()) {
                holder.lockImageView.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (holder.lockImageView.getVisibility() == View.INVISIBLE) {
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
                    }
                    // Return true to indicate the long click was handled
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return (exercises != null ? exercises.size() : 0);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setExercises(List<Exercise> exercises) {
            this.exercises = exercises;
            notifyDataSetChanged();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            Collections.swap(exercises, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            Exercise exercise = exercises.get(position);
            if (!exercise.isLocked()) {
                Utilities.deleteFile(exercise.getPath(), exercise.getId());
                exercises.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    private static class ExercisesViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView lockImageView;
        private final TextView nameView;

        public ExercisesViewHolder(ItemExercisesBinding binding,
                                   ExercisesRecyclerViewInterface recyclerViewInterface) {
            super(binding.getRoot());
            imageView = binding.imageViewItemExercises;
            nameView = binding.textViewItemExercises;
            lockImageView = binding.lockImageView;


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
        Intent intent = new Intent(getContext(), ExerciseActivity.class);
        intent.putExtra("savedInstanceState", savedState);
        launcher.launch(intent);
    }
}