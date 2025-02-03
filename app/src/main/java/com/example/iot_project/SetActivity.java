package com.example.iot_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.iot_project.classes.Exercise;
import com.example.iot_project.classes.Set;
import com.google.android.material.textfield.TextInputEditText;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Objects;

public class SetActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> launcher;
    private NumberPicker cyclesNumberPicker;
    private NumberPicker repetitionsNumberPicker;
    private Button btnChooseExercise;
    private ImageView exerciseIcon;
    private TextInputEditText pauseCyclesEditText;
    private int iconId;
    private boolean isModification;
    private Exercise setExercise;
    private int setPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        // Create the launcher and register for activity result
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result here
                        Intent data = result.getData();
                    }
                });

        cyclesNumberPicker = findViewById(R.id.set_cycles_number_picker);
        repetitionsNumberPicker = findViewById(R.id.set_repetitions_number_picker);

        // Find elements by their IDs
        Button btnSaveNewSet = findViewById(R.id.btnSaveNewSet);
        Button btnResetNewSet = findViewById(R.id.btnResetNewSet);
        Button btnCancelNewSet = findViewById(R.id.btnCancelNewSet);
        TextView pauseCyclesText = findViewById(R.id.pause_cycles_text);
        pauseCyclesEditText = findViewById(R.id.pauseCyclesEditText);

        btnChooseExercise = findViewById(R.id.btnChooseExercise);
        iconId = R.drawable.ic_launcher_background;
        exerciseIcon = findViewById(R.id.setExerciseImageView);
        exerciseIcon.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        isModification = intent.getBooleanExtra("isModification", false);

        if (isModification) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Set");
            }

            Set set = (Set) intent.getSerializableExtra("setObject");
            setPosition = intent.getIntExtra("position", 0);
            cyclesNumberPicker.setValue(set.getCycles());
            repetitionsNumberPicker.setValue(set.getRepetitions());
            btnChooseExercise.setVisibility(View.INVISIBLE);
            exerciseIcon.setVisibility(View.VISIBLE);
            setExercise = set.getExercise();
            iconId = setExercise.getImage();
            exerciseIcon.setImageResource(iconId);
            pauseCyclesEditText.setText(String.format(Integer.toString(
                    set.getPauseBetweenCycles())));

        }
        else {
            savedInstanceState = intent.getBundleExtra("savedInstanceState");
        }

        // Restore state
        if (!isModification && savedInstanceState != null && !savedInstanceState.isEmpty()) {
            cyclesNumberPicker.setValue(savedInstanceState.getInt("cycles", 1));
            repetitionsNumberPicker.setValue(savedInstanceState.getInt("repetitions", 1));
            iconId = savedInstanceState.getInt("icon", R.drawable.ic_launcher_background);
            exerciseIcon.setVisibility(savedInstanceState.getInt("icon_visible", View.INVISIBLE));
            if (exerciseIcon.getVisibility() == View.VISIBLE) {
                exerciseIcon.setImageResource(iconId);
                btnChooseExercise.setVisibility(View.INVISIBLE);
            }
            pauseCyclesEditText.setText(savedInstanceState.getString("pause"));
        }

        // Set onClickListener for each button
        btnSaveNewSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        btnResetNewSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        btnCancelNewSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        btnChooseExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExercisesActivity.class);
                launcher.launch(intent);
            }
        });

        exerciseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ExercisesActivity.class);
                launcher.launch(intent);
            }
        });

        pauseCyclesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseCyclesText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light_gray));
                pauseCyclesEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(pauseCyclesEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        pauseCyclesEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    pauseCyclesEditText.clearFocus();
                    pauseCyclesText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light_gray));
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            setExercise = (Exercise) data.getSerializableExtra("exercise");
            iconId = setExercise.getImage();
            exerciseIcon.setImageResource(iconId);
            exerciseIcon.setVisibility(View.VISIBLE);
            btnChooseExercise.setVisibility(View.INVISIBLE);
            TextView exerciseText = findViewById(R.id.set_exercise_text);
            exerciseText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.light_gray));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("cycles", cyclesNumberPicker.getValue());
        outState.putInt("repetitions", repetitionsNumberPicker.getValue());
        outState.putInt("icon", iconId);
        outState.putInt("icon_visible", exerciseIcon.getVisibility());
        outState.putString("pause", Objects.requireNonNull(pauseCyclesEditText.getText()).toString());
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

    private void save() {
        // Pass the selected icon ID back to the new training activity
        if (btnChooseExercise.getVisibility() == View.VISIBLE) {
            TextView exerciseText = findViewById(R.id.set_exercise_text);
            exerciseText.setTextColor(Color.RED);
        }
        else if (Objects.requireNonNull(pauseCyclesEditText.getText()).toString().isEmpty()) {
            TextView pauseText = findViewById(R.id.pause_cycles_text);
            pauseText.setTextColor(Color.RED);
        }
        else {
            Set set = new Set(null, setExercise.getName(), setExercise,
                    cyclesNumberPicker.getValue(), repetitionsNumberPicker.getValue(),
                    Integer.parseInt(Objects.requireNonNull(pauseCyclesEditText.getText()).toString()));
            Intent intent = new Intent();
            intent.putExtra("setObject", set);
            intent.putExtra("isModification", isModification);
            intent.putExtra("position", setPosition);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void reset() {
        btnChooseExercise.setVisibility(View.VISIBLE);
        exerciseIcon.setVisibility(View.INVISIBLE);
        cyclesNumberPicker.setValue(1);
        repetitionsNumberPicker.setValue(1);
        pauseCyclesEditText.setText("");
    }

    private void cancel() {
//        Bundle savedInstanceState = new Bundle();
//        Intent intent = new Intent();
//        intent.putExtra("savedInstanceState", savedInstanceState);
//        setResult(Activity.RESULT_CANCELED, intent);
//        super.onBackPressed();
        if (isModification)
            finish();
        else
            onBackPressed();
    }
}
