package com.example.iot_project.classes;

import android.os.CountDownTimer;

public abstract class CountUpTimer extends CountDownTimer {
    private static final int INTERVAL_MS = 10; // The interval in milliseconds
    private final long duration;

    public CountUpTimer(long duration) {
        super(duration, INTERVAL_MS);
        this.duration = duration;
    }

    public abstract void onTick(int second, int millisecond);

    @Override
    public void onTick(long millisUntilFinished) {
        int elapsedSeconds = (int) ((duration - millisUntilFinished) / 1000);
        int elapsedMilliseconds = (int) ((duration - millisUntilFinished) % 1000);
        onTick(elapsedSeconds, elapsedMilliseconds);
    }
}

