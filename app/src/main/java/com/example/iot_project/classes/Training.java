package com.example.iot_project.classes;

import android.content.Context;

import com.example.iot_project.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Training implements Serializable {
    private final String path;
    private final String id;
    private String name;
    private String description;
    private Integer image;
    private List<Set> sets;
    private Integer pauseBetweenSets;

    public Training(String name, String description, Integer image, Integer pauseBetweenSets, Context context) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.pauseBetweenSets = pauseBetweenSets;
        sets = new ArrayList<>();
        this.path = context.getFilesDir().getPath() + "/" + Constants.trainingsDir;

        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();
    }

    public void addSet(Set set){
        sets.add(set);
    }

    public void changeSetPosition(int currentPosition, int newPosition) {
        Set set = sets.get(currentPosition);
        sets.remove(currentPosition);
        sets.add(newPosition, set);
    }

    public void save() throws IOException {
        Utilities.saveObject(this, path + "/" + id);
    }

    public static Training load(String path) throws IOException, ClassNotFoundException {
        return (Training) Utilities.loadObject(path);
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

    public String getName() {
        return name;
    }

    public List<Set> getSets() {
        return sets;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
    @SuppressWarnings("unchecked")
    public void setSets(List<Set> sets) {
        this.sets = (List<Set>) Utilities.deepCopy(sets);
    }

    public Integer getPauseBetweenSets() {
        return pauseBetweenSets;
    }

    public void setPauseBetweenSets(Integer pauseBetweenSets) {
        this.pauseBetweenSets = pauseBetweenSets;
    }
}
