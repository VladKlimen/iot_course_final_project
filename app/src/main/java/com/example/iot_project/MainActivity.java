package com.example.iot_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;

import com.example.iot_project.classes.DateTimePickerFragment;
import com.example.iot_project.classes.Exercise;
import com.example.iot_project.ui.exercises.ExercisesFragment;
import com.example.iot_project.ui.scheduled.ScheduledTrainingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iot_project.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
        implements DateTimePickerFragment.OnDateTimeSetListener {
    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(int year, int month, int day, int hour, int minute);
    }

    private AppBarConfiguration mAppBarConfiguration;
    private OnDateTimeSelectedListener onDateTimeSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        if (binding.appBarMain.newItemButton != null) {
            binding.appBarMain.newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNewItemButtonClick(binding.appBarMain.contentMain.navHostFragmentContentMain.
                            getFragment().getChildFragmentManager().getFragments().get(0), binding.appBarMain.newItemButton);
                }
            });
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_scheduled, R.id.nav_exercises, R.id.nav_statistics, R.id.nav_trainings, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_scheduled, R.id.nav_exercises, R.id.nav_trainings, R.id.nav_statistics)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        // dynamic background
        AnimationDrawable animationDrawable =
                (AnimationDrawable) findViewById(R.id.mainActivityContainer).getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onDateTimeSet(int year, int month, int day, int hour, int minute) throws IOException {
        if (onDateTimeSelectedListener != null) {
            onDateTimeSelectedListener.onDateTimeSelected(year, month, day, hour, minute);
        }
    }

    public void setOnDateTimeSelectedListener(OnDateTimeSelectedListener listener) {
        this.onDateTimeSelectedListener = listener;
    }

    public void onNewItemButtonClick(Fragment fragment, FloatingActionButton fab) {
        if (fragment instanceof ScheduledTrainingsFragment) {
            ScheduledTrainingsFragment scheduledTrainingsFragment = (ScheduledTrainingsFragment) fragment;
            scheduledTrainingsFragment.onNewItemButtonCLick(fab);
        }
        else if (fragment instanceof ExercisesFragment) {
            ExercisesFragment exercisesFragment = (ExercisesFragment) fragment;
            exercisesFragment.onNewItemButtonCLick();
        }
        else if (fragment instanceof TrainingsFragment) {
            TrainingsFragment trainingsFragment = (TrainingsFragment) fragment;
            trainingsFragment.onNewItemButtonCLick();
        }

    }
}