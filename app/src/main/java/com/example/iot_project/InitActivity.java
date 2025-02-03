package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.iot_project.classes.Exercise;

import pl.droidsonroids.gif.GifDrawable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pl.droidsonroids.gif.GifImageView;

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        requestPermissions();

        File folder = new File(getFilesDir().getPath() + "/" + Constants.builtInDir);
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            String builtInFileName = "built_in.csv";
            readExercisesFromCSV(builtInFileName);
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        GifDrawable gifDrawable = (GifDrawable) ((GifImageView) findViewById(R.id.launcherGif)).getDrawable();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // check devices and login settings and start the corresponding activity
                Intent intent;
                File directory = new File(getFilesDir(), Constants.devicesDir);
                File[] files = directory.listFiles();
                if (files == null) {
                    intent = new Intent(getBaseContext(), DevicesActivity.class);
                } else {
                    intent = new Intent(getBaseContext(), MainActivity.class);
                }
                startActivity(intent);
                // close this activity
                finish();
            }
        }, gifDrawable.getDuration());
        // TODO: uncomment the above and delete this:
//        Intent intent;
//        File directory = new File(getFilesDir(), Constants.devicesDir);
//        File[] files = directory.listFiles();
//        if (files == null) {
//            intent = new Intent(getBaseContext(), DevicesActivity.class);
//        } else {
//            intent = new Intent(getBaseContext(), MainActivity.class);
//        }
//        startActivity(intent);

    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.BLUETOOTH}, 0);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.BLUETOOTH_ADMIN}, 2);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.BLUETOOTH_SCAN}, 3);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        android.Manifest.permission.ACCESS_FINE_LOCATION}, 4);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 5);
            }
        }
    }

    private void readExercisesFromCSV(String filename) {
        AssetManager manager = getAssets();
        InputStream file;
        String name;
        String description;
        float height;
        float threshold;
        float prominence;

        Resources resources = getResources();

        try {
            file = manager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine(); // Skip header line

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";");

                if (tokens.length > 0) {
                    name = tokens[0];
                    description = tokens[1];

                    @SuppressLint("DiscouragedApi")
                    final int image = resources.getIdentifier(tokens[2], "drawable",
                            getPackageName());

                    height = Float.parseFloat(tokens[3]);
                    threshold = Float.parseFloat(tokens[4]);
                    prominence = Float.parseFloat(tokens[5]);
                    Exercise exercise = new Exercise(name, description, image,
                            height, threshold, prominence, getBaseContext());
                    exercise.setLocked(true);   // lock built in exercises for editing and deleting
                    exercise.saveBuiltIn();
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.w("ReadCSV built in exercises", e);
        }
    }

}