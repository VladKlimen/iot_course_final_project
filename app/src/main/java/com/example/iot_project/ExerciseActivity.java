package com.example.iot_project;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import com.example.iot_project.classes.Exercise;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;

public class ExerciseActivity extends AppCompatActivity {

    private Bundle savedState = new Bundle();
    private Exercise exercise;
    private ActivityResultLauncher<Intent> launcher;
    private TextInputEditText exerciseNameText;
    private TextInputEditText  exerciseDescriptionText;
    private Button btnChooseExercise;
    private Button btnCalibrate;
    private ImageView exerciseIcon;
    private int iconId;
    private boolean isModification = false;
    private boolean fromFragment = false;
    private Float modelHeight = (float) 0;
    private Float modelThreshold = (float) 0;
    private Float modeProminence = (float) 0;
    private boolean calibrated = false;
    private boolean isToastShown = false;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        // Restrict text length
        restrictTextLength(R.id.nameTextContainerExercise, 30);
        restrictTextLength(R.id.descriptionTextContainerExercise, 100);

        // Find elements by their IDs
        Button btnSaveNewExercise = findViewById(R.id.btnSaveNewExercise);
        Button btnResetNewExercise = findViewById(R.id.btnResetNewExercise);
        Button btnCancelNewExercise = findViewById(R.id.btnCancelNewExercise);

        exerciseNameText = findViewById(R.id.exerciseNameEditText);
        exerciseDescriptionText = findViewById(R.id.exerciseDescriptionTextMultiLine);
        exerciseDescriptionText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        exerciseDescriptionText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        btnCalibrate = findViewById(R.id.calibrate_button);
        btnChooseExercise = findViewById(R.id.btnChooseExercise);
        iconId = R.drawable.ic_launcher_background;
        exerciseIcon = findViewById(R.id.exerciseImageView);
        exerciseIcon.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        isModification = intent.getBooleanExtra("isModification", false);
        fromFragment = intent.getBooleanExtra("fromFragment", false);

        if (isModification) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Exercise");
            }

            btnChooseExercise.setVisibility(View.INVISIBLE);
            exerciseIcon.setVisibility(View.VISIBLE);

            if(fromFragment) {
                exercise = (Exercise) intent.getSerializableExtra("exercise");
                position = intent.getIntExtra("position", 0);
                exerciseNameText.setText(exercise.getName());
                exerciseDescriptionText.setText(exercise.getDescription());
                iconId = exercise.getImage();
                modelHeight = exercise.getModelHeight();
                modelThreshold = exercise.getModelThreshold();
                modeProminence = exercise.getModelProminence();
                calibrated = true;
            }
            else {
                exerciseNameText.setText(intent.getStringExtra("name"));
                exerciseDescriptionText.setText(intent.getStringExtra("description"));
                iconId = intent.getIntExtra("icon", iconId);
            }
            exerciseIcon.setImageResource(iconId);
        }
        else {
            savedInstanceState = intent.getBundleExtra("savedInstanceState");
        }

        // Restore state
        if (!isModification && savedInstanceState != null && !savedInstanceState.isEmpty()) {
            exerciseNameText.setText(savedInstanceState.getString("name", ""));
            exerciseDescriptionText.setText(savedInstanceState.getString("description", ""));
            iconId = savedInstanceState.getInt("icon", R.drawable.ic_launcher_background);
            exerciseIcon.setVisibility(savedInstanceState.getInt("icon_visible", View.INVISIBLE));

            modelHeight = savedInstanceState.getFloat("height");
            modelThreshold = savedInstanceState.getFloat("threshold");
            modeProminence = savedInstanceState.getFloat("prominence");
            calibrated = savedInstanceState.getBoolean("calibrated");

            if (exerciseIcon.getVisibility() == View.VISIBLE) {
                exerciseIcon.setImageResource(iconId);
                btnChooseExercise.setVisibility(View.INVISIBLE);
            }
        }

        exerciseDescriptionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    v.clearFocus();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        // Set onClickListener for each button
        btnSaveNewExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnResetNewExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        btnCancelNewExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExerciseTrainModelActivity.class);
                launcher.launch(intent);
            }
        });

        btnChooseExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExerciseIconsActivity.class);
                launcher.launch(intent);
            }
        });

        exerciseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExerciseIconsActivity.class);
                launcher.launch(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getBooleanExtra("icon", false)) {
                iconId = data.getIntExtra("selectedIconId", R.drawable.ic_launcher_background);
                exerciseIcon.setImageResource(iconId);
                exerciseIcon.setVisibility(View.VISIBLE);
                btnChooseExercise.setVisibility(View.INVISIBLE);
                TextView exerciseIconText = findViewById(R.id.exercise_text);
                exerciseIconText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light_gray));
            }
            else {
                modelHeight = data.getFloatExtra("height", 0);
                modelThreshold = data.getFloatExtra("threshold", 0);
                modeProminence = data.getFloatExtra("prominence", 0);
                calibrated = data.getBooleanExtra("calibrated", false);
                if (calibrated) {
                    btnCalibrate.setTextColor(Color.BLACK);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("name", Objects.requireNonNull(exerciseNameText.getText()).toString());
        outState.putString("description", Objects.requireNonNull(exerciseDescriptionText.getText()).toString());
        outState.putInt("icon", iconId);
        outState.putInt("icon_visible", exerciseIcon.getVisibility());
        outState.putFloat("height", modelHeight);
        outState.putFloat("threshold", modelThreshold);
        outState.putFloat("prominence", modeProminence);
        outState.putBoolean("calibrated", calibrated);
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

    private void save() throws IOException {
        String name, description = "";
        boolean ok = true;
        name = Objects.requireNonNull(exerciseNameText.getText()).toString();
        if (name.equals("")) {
            TextInputLayout nameTextContainer = findViewById(R.id.nameTextContainerExercise);
            nameTextContainer.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
            ok = false;
        }
        if (btnChooseExercise.getVisibility() == View.VISIBLE) {
            TextView exerciseText = findViewById(R.id.exercise_text);
            exerciseText.setTextColor(Color.RED);
            ok = false;
        }
        if (!calibrated) {
            btnCalibrate.setTextColor(Color.RED);
            ok = false;
        }

        if (exerciseDescriptionText.getText() != null) {
            description = exerciseDescriptionText.getText().toString();
        }

        if (ok) {
            Intent intent = new Intent();
            if (!fromFragment) {
                exercise = new Exercise(name, description, iconId,
                        modelHeight, modelThreshold, modeProminence, getBaseContext());
                intent.putExtra("new", true);
            }
            else {
                exercise.setName(name);
                exercise.setDescription(description);
                exercise.setImage(iconId);
                exercise.setModelHeight(modelHeight);
                exercise.setModelThreshold(modelThreshold);
                exercise.setModelProminence(modeProminence);
                intent.putExtra("position", position);
            }
            exercise.save();

            intent.putExtra("exercise", exercise);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void reset() {
        exerciseNameText.setText("");
        TextInputLayout nameTextContainer = findViewById(R.id.nameTextContainerExercise);
        nameTextContainer.setError(null);
        exerciseDescriptionText.setText("");
        TextInputLayout descriptionTextContainer = findViewById(R.id.descriptionTextContainerExercise);
        descriptionTextContainer.setError(null);
        TextView exerciseText = findViewById(R.id.exercise_text);
        exerciseText.setTextColor(getResources().getColor(R.color.light_gray, null));
        btnCalibrate.setTextColor(Color.BLACK);
        btnChooseExercise.setVisibility(View.VISIBLE);
        exerciseIcon.setVisibility(View.INVISIBLE);
        modelHeight = modelThreshold = modeProminence = (float) 0;
        calibrated = false;
    }
    private void cancel() {
        onBackPressed();
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
}