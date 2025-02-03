package com.example.iot_project.ui.scheduled;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.MainActivity;
import com.example.iot_project.RepetitionsCounterActivity;
import com.example.iot_project.TrainingActivity;
import com.example.iot_project.R;
import com.example.iot_project.TrainingsActivity;
import com.example.iot_project.classes.DateTimePickerFragment;
import com.example.iot_project.classes.ItemTouchHelperCallback;
import com.example.iot_project.classes.ScheduledTraining;
import com.example.iot_project.classes.Training;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.databinding.FragmentScheduledTrainingsBinding;
import com.example.iot_project.databinding.ItemScheduledTrainingsBinding;
import com.example.iot_project.interfaces.ItemTouchHelperAdapter;
import com.example.iot_project.interfaces.ScheduledRecyclerViewInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ScheduledTrainingsFragment extends Fragment implements ScheduledRecyclerViewInterface {

    private FragmentScheduledTrainingsBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private Bundle savedState = new Bundle();
    private ScheduledAdapter adapter;
    private ScheduledTrainingsViewModel scheduledTrainingsViewModel;

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
                                ScheduledTraining scheduledTraining;
                                if (data.getBooleanExtra("switchTraining", false)) {
                                    Training training = (Training) data.getSerializableExtra("training");
                                    int position = data.getIntExtra("position", 0);
                                    scheduledTraining = Objects.requireNonNull(scheduledTrainingsViewModel.
                                                    getScheduledTrainings().getValue()).get(position);
                                    scheduledTraining.setTraining(training);
                                    adapter.notifyItemChanged(position);
                                }
                                else {
                                    scheduledTraining = (ScheduledTraining)
                                            data.getSerializableExtra("scheduledTraining");
                                    scheduledTrainingsViewModel.addScheduledTraining(scheduledTraining);
                                    adapter.notifyItemInserted(adapter.getItemCount());
                                }
                                try {
                                    scheduledTraining.save();
                                    if (data.getBooleanExtra("new", false)) {
                                        scheduledTraining.getTraining().save();
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        scheduledTrainingsViewModel = new ViewModelProvider(this).get(ScheduledTrainingsViewModel.class);

        binding = FragmentScheduledTrainingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewScheduled;
        adapter = new ScheduledAdapter(scheduledTrainingsViewModel.getScheduledTrainings().getValue(),
                this, getParentFragmentManager(), launcher, getContext());
        recyclerView.setAdapter(adapter);
        // Register an observer
        scheduledTrainingsViewModel.getScheduledTrainings().observe(getViewLifecycleOwner(), scheduledTrainings -> {
            adapter.setScheduledTrainings(scheduledTrainings);
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            ((MainActivity) context).setOnDateTimeSelectedListener(this::handleDateTimeSelected);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {
        ScheduledTraining scheduledTraining =
                Objects.requireNonNull(scheduledTrainingsViewModel.
                        getScheduledTrainings().getValue()).get(position);
        Intent intent = new Intent(getContext(), RepetitionsCounterActivity.class);
        intent.putExtra("training", scheduledTraining.getTraining());
        intent.putExtra("position", position);
        intent.putExtra("scheduled", true);
        launcher.launch(intent);
    }

    public void handleDateTimeSelected(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        Date dateTime = calendar.getTime();

        ScheduledTraining scheduledTraining = adapter.getItem(adapter.selectedPosition);
        scheduledTraining.setDateTime(dateTime);
        try {
            scheduledTraining.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        adapter.notifyItemChanged(adapter.selectedPosition);
    }

    private static class ScheduledAdapter extends RecyclerView.Adapter<ScheduledViewHolder>
            implements ItemTouchHelperAdapter {
        private final ScheduledRecyclerViewInterface recyclerViewInterface;
        private List<ScheduledTraining> scheduledTrainings;
        private final FragmentManager fragmentManager;
        private final ActivityResultLauncher<Intent> launcher;
        private final Context context;
        private int selectedPosition;

        protected ScheduledAdapter(List<ScheduledTraining> scheduledTrainings,
                                   ScheduledRecyclerViewInterface recyclerViewInterface,
                                   FragmentManager fragmentManager,
                                   ActivityResultLauncher<Intent> launcher, Context context) {
            this.scheduledTrainings = scheduledTrainings;
            this.recyclerViewInterface = recyclerViewInterface;
            this.fragmentManager = fragmentManager;
            this.launcher = launcher;
            this.context = context;
        }

        @NonNull
        @Override
        public ScheduledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemScheduledTrainingsBinding binding = ItemScheduledTrainingsBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new ScheduledViewHolder(binding, recyclerViewInterface);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledViewHolder holder, int position) {
            holder.descriptionView.setText(scheduledTrainings.get(position).getName());
            assert holder.dateView != null;
            holder.dateView.setText(scheduledTrainings.get(position).getDateTimeStr());
            holder.imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(holder.imageView.getResources(),
                            scheduledTrainings.get(position).getImage(),
                            null));

            assert holder.dateTimeImageView != null;
            holder.dateTimeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = holder.getBindingAdapterPosition();
                    DialogFragment newFragment = new DateTimePickerFragment();
                    newFragment.show(fragmentManager, "dateTimePicker");
                }
            });

            assert holder.editTrainingImageView != null;
            holder.editTrainingImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = holder.getBindingAdapterPosition();
                    Intent intent = new Intent(context, TrainingsActivity.class);
                    intent.putExtra("switchTraining", true);
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
            return (scheduledTrainings != null ? scheduledTrainings.size() : 0);
        }

        public ScheduledTraining getItem(int position) {
            return scheduledTrainings.get(position);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setScheduledTrainings(List<ScheduledTraining> scheduledTrainings) {
            this.scheduledTrainings = scheduledTrainings;
            notifyDataSetChanged();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            Collections.swap(scheduledTrainings, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            ScheduledTraining scheduledTraining = scheduledTrainings.get(position);
            Utilities.deleteFile(scheduledTraining.getPath(), scheduledTraining.getId());
            scheduledTrainings.remove(position);
            notifyItemRemoved(position);
        }
    }

    private static class ScheduledViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView descriptionView;
        private final TextView dateView;
        private final ImageView editTrainingImageView;
        private final ImageView dateTimeImageView;

        public ScheduledViewHolder(ItemScheduledTrainingsBinding binding,
                                   ScheduledRecyclerViewInterface recyclerViewInterface) {
            super(binding.getRoot());
            imageView = binding.imageViewItemScheduled;
            descriptionView = binding.textViewItemScheduled;
            dateView = binding.textViewScheduledDateTime;
            editTrainingImageView = binding.editScheduledImageView;
            dateTimeImageView = binding.dateTimeImageView;

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

    public void onNewItemButtonCLick(FloatingActionButton fab) {
//        // Creating the instance of PopupMenu
//        PopupMenu popup = new PopupMenu(getContext(), fab);
//        // Inflating the Popup using XML file
//        popup.getMenuInflater().inflate(R.menu.new_scheduled_button_menu, popup.getMenu());
//
//        // registering popup with OnMenuItemClickListener
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == R.id.action_add_new_training) {
//                    // Code to start the Add New Training Activity
//                    Intent intent = new Intent(getContext(), TrainingActivity.class);
//                    intent.putExtra("savedInstanceState", savedState);
//                    launcher.launch(intent);
//                    return true;
//                } else if (item.getItemId() == R.id.action_add_existing_training) {
//                    // Code to start the Add Existing Training Activity
//                    Intent intent = new Intent(getContext(), TrainingsActivity.class);
//                    launcher.launch(intent);
//                    return true;
//                }
//                return false;
//            }
//        });
//        popup.show();
        Intent intent = new Intent(getContext(), TrainingsActivity.class);
        intent.putExtra("scheduled", true);
        launcher.launch(intent);
    }
}