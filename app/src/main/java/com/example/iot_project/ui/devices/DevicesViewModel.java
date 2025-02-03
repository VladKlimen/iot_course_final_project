package com.example.iot_project.ui.devices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {
    private final MutableLiveData<String> selectedDevice = new MutableLiveData<>();

    public void selectDevice(String deviceAddress) {
        selectedDevice.setValue(deviceAddress);
    }

    public MutableLiveData<String> getSelectedDevice() {
        return selectedDevice;
    }
}