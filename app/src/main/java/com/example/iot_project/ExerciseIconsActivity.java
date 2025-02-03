package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.iot_project.classes.IconAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseIconsActivity extends AppCompatActivity implements IconAdapter.IconClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_icons);

        RecyclerView iconGridRecyclerView = findViewById(R.id.iconGridRecyclerView);
        iconGridRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Create a list of icon IDs
        List<Integer> iconList = Arrays.asList(
                R.drawable.run, R.drawable.walk,
                R.drawable.exercise_1, R.drawable.exercise_2,
                R.drawable.exercise_3, R.drawable.exercise_4,
                R.drawable.exercise_5, R.drawable.exercise_6,
                R.drawable.exercise_7, R.drawable.exercise_8,
                R.drawable.exercise_9, R.drawable.exercise_10,
                R.drawable.exercise_11, R.drawable.exercise_12,
                R.drawable.exercise_13, R.drawable.exercise_14,
                R.drawable.exercise_15, R.drawable.exercise_16,
                R.drawable.jumping, R.drawable.pushups,
                R.drawable.running, R.drawable.running_1,
                R.drawable.stretching, R.drawable.stretching_1,
                R.drawable.stretching_2, R.drawable.stretching_3,
                R.drawable.stretching_4, R.drawable.stretching_5,
                R.drawable.weightlifting, R.drawable.weightlifting_1,
                R.drawable.weightlifting_2, R.drawable.weightlifting_3,
                R.drawable.weightlifting_4, R.drawable.weightlifting_5,
                R.drawable.weightlifting_6, R.drawable.weightlifting_7,
                R.drawable.weightlifting_8, R.drawable.weightlifting_9,
                R.drawable.yoga, R.drawable.hiking);

        // Create an adapter with the icon list and set the click listener
        IconAdapter iconAdapter = new IconAdapter(iconList, this);
        iconGridRecyclerView.setAdapter(iconAdapter);
    }

    @Override
    public void onIconClick(int iconId) {
        // Pass the selected icon ID back to the new training activity
        Intent intent = new Intent();
        intent.putExtra("selectedIconId", iconId);
        intent.putExtra("icon", true);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
