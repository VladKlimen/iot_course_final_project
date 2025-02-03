package com.example.iot_project.classes;

import android.widget.ImageView;

import java.io.Serializable;

public class Set implements Serializable {
    private final Training parent;
    private String name;
    private Exercise exercise;
    private Integer cycles;
    private Integer repetitions;
    private Integer pauseBetweenCycles;

    public Set(Training parent, String name, Exercise exercise, Integer cycles, Integer repetitions, Integer pauseBetweenCycles) {
        this.parent = parent;
        this.name = name;
        this.exercise = exercise;
        this.cycles = cycles;
        this.repetitions = repetitions;
        this.pauseBetweenCycles = pauseBetweenCycles;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public Integer getCycles() {
        return cycles;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setCycles(Integer cycles) {
        this.cycles = cycles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Training getParent() {
        return parent;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public Integer getPauseBetweenCycles() {
        return pauseBetweenCycles;
    }

    public void setPauseBetweenCycles(Integer pauseBetweenCycles) {
        this.pauseBetweenCycles = pauseBetweenCycles;
    }

    public Integer getImage() {
        return exercise.getImage();
    }
}
