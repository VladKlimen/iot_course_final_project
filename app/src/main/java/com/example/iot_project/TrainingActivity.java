package com.example.iot_project;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.classes.DateTimePickerFragment;
import com.example.iot_project.classes.ItemTouchHelperCallback;
import com.example.iot_project.classes.ScheduledTraining;
import com.example.iot_project.classes.Set;
import com.example.iot_project.classes.SetAdapter;
import com.example.iot_project.classes.Training;
import com.example.iot_project.interfaces.NewTrainingRecyclerViewInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TrainingActivity extends AppCompatActivity
        implements DateTimePickerFragment.OnDateTimeSetListener, NewTrainingRecyclerViewInterface {

    private Training training;
    private Bundle savedState = new Bundle();
    private RecyclerView recyclerView;
    private SetAdapter setAdapter;
    private ActivityResultLauncher<Intent> launcher;
    private boolean isToastShown = false;
    private boolean isModification = false;
    private Date dateTime;
//    private TextInputEditText dateTimeText;
    private TextInputEditText  trainingNameText;
    private TextInputEditText  trainingDescriptionText;
    private TextInputEditText pauseBetweenSets;
    private TextView setsText;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        // Find elements by their IDs
        Button btnSaveNewTraining = findViewById(R.id.btnSaveNewTraining);
        Button btnResetNewTraining = findViewById(R.id.btnResetNewTraining);
        Button btnCancelNewTraining = findViewById(R.id.btnCancelNewTraining);
        FloatingActionButton fab = findViewById(R.id.new_set_button);

        trainingNameText = findViewById(R.id.trainingNameEditText);
        trainingDescriptionText = findViewById(R.id.trainingDescriptionTextMultiLine);
        trainingDescriptionText.setRawInputType(InputType.TYPE_CLASS_TEXT); //?
//        ImageView dateTimeImageView = findViewById(R.id.dateTimeImageView);
//        dateTimeText = findViewById(R.id.editTextDateTime);
        setsText = findViewById(R.id.training_sets_text);
        pauseBetweenSets = findViewById(R.id.trainingPauseSetsEditText);

        Intent intent = getIntent();

        isModification = intent.getBooleanExtra("isModification", false);

        if (isModification) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Training");
            }

            training =  (Training) intent.getSerializableExtra("training");

            trainingNameText.setText(training.getName());
            trainingDescriptionText.setText(training.getDescription());
            pauseBetweenSets.setText(String.format(training.getPauseBetweenSets().toString()));
//            dateTime = scheduledTraining.getDateTime();
//            dateTimeText.setText(scheduledTraining.getDateTimeStr());
            position = intent.getIntExtra("position", 0);
        }
        else {
            training = new Training("Training", "Description",
                    R.drawable.ic_launcher_background, 0, getBaseContext());
            savedInstanceState = getIntent().getBundleExtra("savedInstanceState");
        }

        // Restore state
        if (!isModification && savedInstanceState != null && !savedInstanceState.isEmpty()) {
            trainingNameText.setText(savedInstanceState.getString("training_name", ""));
            trainingDescriptionText.setText(savedInstanceState.getString("training_description", ""));
            pauseBetweenSets.setText(savedInstanceState.getString("pause"));
            Serializable serializable = savedInstanceState.getSerializable("training_sets");
            @SuppressWarnings("unchecked")
            List<Set> sets = (List<Set>) serializable;
            training.setSets(sets);
        }

        // Initialize the RecyclerView and its adapter
        setAdapter = new SetAdapter(training.getSets(), this);
        recyclerView = findViewById(R.id.trainingSetsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(setAdapter);

        // Restrict text length
        restrictTextLength(R.id.nameTextContainerTraining, 30);
        restrictTextLength(R.id.descriptionTextContainerTraining, 100);

        // Sets replacing and removing handler
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(setAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        trainingDescriptionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    pauseBetweenSets.requestFocus();
                    assert imm != null;
                    imm.showSoftInput(pauseBetweenSets, InputMethodManager.SHOW_IMPLICIT);
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        pauseBetweenSets.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), SetActivity.class);
                intent.putExtra("savedInstanceState", savedState);
                intent.putExtra("isModification", false);
                launcher.launch(intent);
            }
        });

        pauseBetweenSets.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputLayout textContainer = findViewById(R.id.pauseSetsTextContainerTraining);
                textContainer.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.light_gray)));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set onClickListener for each button
        btnSaveNewTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnResetNewTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        btnCancelNewTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Set set = (Set) data.getSerializableExtra("setObject");
            if (data.getBooleanExtra("isModification", false)) {
                int position = data.getIntExtra("position", training.getSets().size() - 1);
                training.getSets().set(position, set);
                setAdapter.notifyItemChanged(position);
            }
            else {
                training.addSet(set);
                int position = training.getSets().size()-1;
                setAdapter.notifyItemInserted(position);
                recyclerView.smoothScrollToPosition(position);
                if (setsText.getCurrentTextColor() == Color.RED) {
                    setsText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light_gray));
                }
            }
            savedState = new Bundle();
        }
        if (resultCode == Activity.RESULT_CANCELED && data != null) {
            savedState = data.getBundleExtra("savedInstanceState");
        }
    }

    @Override
    public void onDateTimeSet(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        dateTime = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateString = formatter.format(dateTime);
    }

    private void restrictTextLength(int textInputLayoutId, int maxLength) {
        TextInputLayout textInputLayout = findViewById(textInputLayoutId);
        TextInputEditText textInputEditText = (TextInputEditText) textInputLayout.getEditText();

        textInputLayout.setCounterMaxLength(maxLength);

        assert textInputEditText != null;
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.light_gray)));
                if (s.length() > maxLength) {
                    textInputEditText.setError("Max character length is " + maxLength);
                    if (!isToastShown) {
                        Toast.makeText(getApplicationContext(), "Maximum limit reached", Toast.LENGTH_SHORT).show();
                        isToastShown = true;
                    }
                }
                else  if (s.length() < maxLength) { // If text is less than maximum, reset the flag
                    isToastShown = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > maxLength) {
                    s.delete(maxLength, s.length());
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("training_name", Objects.requireNonNull(trainingNameText.getText()).toString());
        outState.putString("training_description", Objects.requireNonNull(trainingDescriptionText.getText()).toString());
//        outState.putString("training_date", Objects.requireNonNull(dateTimeText.getText()).toString());
        outState.putString("pause", Objects.requireNonNull(pauseBetweenSets.getText()).toString());
        outState.putSerializable("training_sets", (Serializable) training.getSets());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!isModification) {
            Bundle savedInstanceState = new Bundle();
            onSaveInstanceState(savedInstanceState);
            Intent intent = new Intent();
            intent.putExtra("savedInstanceState", savedInstanceState);
            setResult(Activity.RESULT_CANCELED, intent);
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClick(int position) {
        Set set = training.getSets().get(position);
        Intent intent = new Intent(getBaseContext(), SetActivity.class);
        intent.putExtra("setObject", set);
        intent.putExtra("isModification", true);
        intent.putExtra("position", position);
        launcher.launch(intent);
    }

    private void save() throws IOException {
        String name = "", description = "";
        boolean ok = true;
        name = Objects.requireNonNull(trainingNameText.getText()).toString();
        if (name.equals("")) {
            TextInputLayout nameTextContainer = findViewById(R.id.nameTextContainerTraining);
            nameTextContainer.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
            ok = false;
        }
        String pause = Objects.requireNonNull(pauseBetweenSets.getText()).toString();
        if (pause.equals("")) {
            TextInputLayout nameTextContainer = findViewById(R.id.pauseSetsTextContainerTraining);
            nameTextContainer.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
            ok = false;
        }
//        if (dateTime == null) {
//            TextInputLayout dateTimeTextContainer = findViewById(R.id.dateTimeTextContainerTraining);
//            dateTimeTextContainer.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
//            ok = false;
//        }
        if (training.getSets().isEmpty()) {
            setsText.setTextColor(Color.RED);
            ok = false;
        }

        if (trainingDescriptionText.getText() != null) {
            description = trainingDescriptionText.getText().toString();
        }

        if (ok) {
            Intent intent = new Intent();
            if (!isModification) {
                intent.putExtra(("new"), true);
            }

            training.setName(name);
            training.setDescription(description);
            training.setImage(training.getSets().get(0).getExercise().getImage());
            training.setPauseBetweenSets(Integer.parseInt(
                    Objects.requireNonNull(pauseBetweenSets.getText()).toString()));
            training.save();

            intent.putExtra("training", training);
            intent.putExtra("isModification", isModification);
            intent.putExtra("position", position);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reset() {
        trainingNameText.setText("");
        TextInputLayout nameTextContainer = findViewById(R.id.nameTextContainerTraining);
        nameTextContainer.setError(null);
        trainingDescriptionText.setText("");
        pauseBetweenSets.setText("");
        TextInputLayout descriptionTextContainer = findViewById(R.id.descriptionTextContainerTraining);
        descriptionTextContainer.setError(null);
//        dateTimeText.setText("");
//        TextInputLayout dateTimeTextContainer = findViewById(R.id.dateTimeTextContainerTraining);
//        dateTimeTextContainer.setError(null);
        training.getSets().clear();
        setsText.setTextColor(getResources().getColor(R.color.light_gray, null));
        setAdapter.notifyDataSetChanged();
    }

    private void cancel() {
        onBackPressed();
    }

}