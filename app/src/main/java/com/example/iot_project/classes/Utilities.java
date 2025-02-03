package com.example.iot_project.classes;

import android.annotation.SuppressLint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utilities {

    public static String getTimeStamp() {
        Date now = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format =
                new SimpleDateFormat("ddMMyyhhmmssMs");
        return format.format(now);
    }

    public static boolean deleteFile(String location, String name) {
        File file = new File(location, name);
        return file.delete();
    }

    public static void saveObject(Object obj, String path) throws IOException {
        File file = new File(path);
        File parentDir = file.getParentFile();
        assert parentDir != null;
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.close();
    }

    public static Object loadObject(String path) throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fin);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    public static Object deepCopy(Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            return new ObjectInputStream(bais).readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double movingAverage(List<Double> values, double newValue, int n) {
        if (values.size() >= n) {
            values.remove(0);
        }
        values.add(newValue);
        return values.stream().mapToDouble(i -> i).average().orElse(0.0);
    }

}
