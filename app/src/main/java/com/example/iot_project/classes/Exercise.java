package com.example.iot_project.classes;

import android.content.Context;

import com.example.iot_project.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class Exercise implements Serializable {
    private final String id;
    private final String path;
    private final String builtinPath;

    private String name;
    private String description;
    private Integer image;

    private Float modelHeight;
    private Float modelThreshold;
    private Float modelProminence;

    private boolean locked;

    public Exercise(String name, String description, Integer image,
                    Float modelHeight, Float modelThreshold, Float modelProminence, Context context) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.path = context.getFilesDir().getPath() + "/" + Constants.exercisesDir;
        this.builtinPath = context.getFilesDir().getPath() + "/" + Constants.builtInDir;

        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();

        this.modelHeight = modelHeight;
        this.modelThreshold = modelThreshold;
        this.modelProminence = modelProminence;
        this.locked = false;
    }

    public void save() throws IOException {
        Utilities.saveObject(this, path + "/" + id);
    }

    public void saveBuiltIn() throws IOException {
        Utilities.saveObject(this, builtinPath + "/" + id);
    }

    public String getDescription() {
        return description;
    }

    public Integer getImage() {
        return image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(Integer image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Float getModelHeight() {
        return modelHeight;
    }

    public void setModelHeight(Float modelHeight) {
        this.modelHeight = modelHeight;
    }

    public Float getModelThreshold() {
        return modelThreshold;
    }

    public void setModelThreshold(Float modelThreshold) {
        this.modelThreshold = modelThreshold;
    }

    public Float getModelProminence() {
        return modelProminence;
    }

    public void setModelProminence(Float modelProminence) {
        this.modelProminence = modelProminence;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
