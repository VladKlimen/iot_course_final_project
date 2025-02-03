package com.example.iot_project.classes;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.iot_project.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ScheduledTraining implements Serializable {
    private Training training;
    private final String path;
    private final String id;
    private Date dateTime;
    private String dateTimeStr;

    public ScheduledTraining(Training training, Date dateTime, Context context) {
        this.training = training;
        this.dateTime = dateTime;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter =
                new SimpleDateFormat("dd.MM.yy HH:mm");
        dateTimeStr = formatter.format(dateTime);
        this.path = context.getFilesDir().getPath() + "/" + Constants.scheduledDir;

        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();
    }

    public void save() throws IOException {
        Utilities.saveObject(this, path + "/" + id);
    }

    public static ScheduledTraining load(String path) throws IOException, ClassNotFoundException {
        return (ScheduledTraining) Utilities.loadObject(path);
    }

    public Training getTraining() {
        return training;
    }


    public String getName() {
        return training.getName();
    }

    public String getDescription() {
        return training.getDescription();
    }

    public Date getDateTime() {
        return dateTime;
    }

    public Integer getImage() {
        return training.getImage();
    }

    public String getDateTimeStr() {
        return dateTimeStr;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter =
                new SimpleDateFormat("dd.MM.yy HH:mm");
        dateTimeStr = formatter.format(this.dateTime);
    }
}
