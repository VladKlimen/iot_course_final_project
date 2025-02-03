package com.example.iot_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.iot_project.classes.Utilities;
import com.example.iot_project.ui.devices.DevicesFragment;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class DevicesActivity extends AppCompatActivity
        implements DevicesFragment.OnDeviceSelectedListener {

    private Button btnLeftDevice;
    private Button btnRightDevice;
    private Button btnConnect;

    private BluetoothDevice leftDevice = null;
    private BluetoothDevice rightDevice = null;

    private boolean leftClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        // dynamic background
        AnimationDrawable animationDrawable =
                (AnimationDrawable) findViewById(R.id.devicesActivityContainer).getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        btnLeftDevice = findViewById(R.id.btnLeftDevice);
        btnRightDevice = findViewById(R.id.btnRightDevice);
        btnConnect = findViewById(R.id.btnConnect);

        btnLeftDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftClicked = true;
                startFragment(savedInstanceState);
            }
        });

        btnRightDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftClicked = false;
                startFragment(savedInstanceState);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getBaseContext().getFilesDir().getPath() + "/" + Constants.devicesDir;
                try {
                    Utilities.saveObject(leftDevice.getAddress(), path + "left");
                    Utilities.saveObject(rightDevice.getAddress(), path + "right");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            btnRightDevice.setVisibility(View.INVISIBLE);
            btnLeftDevice.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.devicesActivityContainer, new DevicesFragment(), "devices")
                    .commitNow();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
//                btnLeftDevice.setEnabled(false);
//                btnRightDevice.setEnabled(false);
//                Toast.makeText(getBaseContext(), "Bluetooth permission denied!", Toast.LENGTH_LONG).show();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device) {
        if (leftClicked) {
            leftDevice = device;
        }
        else {
            rightDevice = device;
        }
        DevicesFragment fragment = (DevicesFragment) getSupportFragmentManager().findFragmentByTag("devices");
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        btnRightDevice.setVisibility(View.VISIBLE);
        btnLeftDevice.setVisibility(View.VISIBLE);
        if (leftDevice != null && rightDevice != null) {
            if (Objects.equals(leftDevice.getAddress(), rightDevice.getAddress())) {
                btnConnect.setVisibility(View.INVISIBLE);
                Toast.makeText(getBaseContext(), "You have to select different devises", Toast.LENGTH_SHORT).show();
            }
            else {
                btnConnect.setVisibility(View.VISIBLE);
            }
        }
    }

}