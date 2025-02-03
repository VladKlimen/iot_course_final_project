package com.example.iot_project;
public class Constants {

    private Constants() {}
    public static final String trainingsDir = "trainings";
    public static final String exercisesDir = "exercises";
    public static final String builtInDir = "built_in";
    public static final String scheduledDir = "scheduled";
    public static final String devicesDir = "devices/";

    public static final String INTENT_ACTION_DISCONNECT_TEMPLATE = "%1$s.Disconnect";
    public static final String NOTIFICATION_CHANNEL_TEMPLATE = "%1$s.Channel";
    public static final String INTENT_CLASS_MAIN_ACTIVITY_TEMPLATE = "%1$s.MainActivity";

    // values have to be unique within each app
    public static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;
    public static final int REQUEST_CODE_BT = 101;

    public static final int MOVING_AVG_WINDOW_SIZE = 10;
    public static final int MIN_SAMPLES = 10;   // TODO: change to 360
}
