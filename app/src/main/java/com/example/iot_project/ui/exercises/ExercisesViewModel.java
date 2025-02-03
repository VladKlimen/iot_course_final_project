package com.example.iot_project.ui.exercises;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.iot_project.Constants;
import com.example.iot_project.classes.Exercise;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExercisesViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Exercise>> exercises;

    public ExercisesViewModel(Application application) {
        super(application);
        exercises = new MutableLiveData<>();
        List<Exercise> exerciseList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadExercises(exerciseList, Constants.builtInDir);
                loadExercises(exerciseList, Constants.exercisesDir);
            }
        }).start();
    }

    public MutableLiveData<List<Exercise>> getExercises() {
        return exercises;
    }

    public void addExercise(Exercise newExercise) {
        List<Exercise> currentExercises = exercises.getValue();
        if (currentExercises != null) {
            currentExercises.add(newExercise);
            exercises.setValue(currentExercises);
        }
    }

    public void loadExercises(List<Exercise> exerciseList, String path) {
        File directory = new File(getApplication().getFilesDir(), path);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    Object obj = ois.readObject();
                    exerciseList.add((Exercise) obj);

                    ois.close();
                    fis.close();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        // Use postValue to update LiveData from a background thread
        exercises.postValue(exerciseList);
    }

}