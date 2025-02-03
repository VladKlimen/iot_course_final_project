package com.example.iot_project;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.iot_project.Constants;
import com.example.iot_project.classes.Training;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class TrainingsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Training>> trainings;

    public TrainingsViewModel(Application application) {
        super(application);
        trainings = new MutableLiveData<>(new ArrayList<>());
        new Thread(new Runnable() {
            @Override
            public void run() {
                File directory = new File(getApplication().getFilesDir(), Constants.trainingsDir);
                File[] files = directory.listFiles();
                List<Training> trainingList = new ArrayList<>();

                if (files != null) {
                    for (File file : files) {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            ObjectInputStream ois = new ObjectInputStream(fis);

                            Object obj = ois.readObject();
                            trainingList.add((Training) obj);

                            ois.close();
                            fis.close();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Use postValue to update LiveData from a background thread
                trainings.postValue(trainingList);
            }
        }).start();
    }

    public LiveData<List<Training>> getTrainings() {
        return trainings;
    }

    public void addTraining(Training newTraining) {
        List<Training> currentTrainings = trainings.getValue();
        if (currentTrainings != null) {
            currentTrainings.add(newTraining);
            trainings.setValue(currentTrainings);
        }
    }
}
