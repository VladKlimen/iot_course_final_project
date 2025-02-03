package com.example.iot_project.ui.scheduled;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.iot_project.Constants;
import com.example.iot_project.classes.ScheduledTraining;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class ScheduledTrainingsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ScheduledTraining>> scheduledTrainings;

    public ScheduledTrainingsViewModel(Application application) {
        super(application);
        scheduledTrainings = new MutableLiveData<>(new ArrayList<>());
        new Thread(new Runnable() {
            @Override
            public void run() {
                File directory = new File(getApplication().getFilesDir(), Constants.scheduledDir);
                File[] files = directory.listFiles();
                List<ScheduledTraining> scheduled = new ArrayList<>();

                if (files != null) {
                    for (File file : files) {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            ObjectInputStream ois = new ObjectInputStream(fis);

                            Object obj = ois.readObject();
                            scheduled.add((ScheduledTraining) obj);

                            ois.close();
                            fis.close();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Use postValue to update LiveData from a background thread
                scheduledTrainings.postValue(scheduled);
            }
        }).start();
    }

    public LiveData<List<ScheduledTraining>> getScheduledTrainings() {
        return scheduledTrainings;
    }

    public void addScheduledTraining(ScheduledTraining newTraining) {
        List<ScheduledTraining> currentTrainings = scheduledTrainings.getValue();
        if (currentTrainings != null) {
            currentTrainings.add(newTraining);
            scheduledTrainings.setValue(currentTrainings);
        }
    }
}
