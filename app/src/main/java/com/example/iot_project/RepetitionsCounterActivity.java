package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.iot_project.classes.CountUpTimer;
import com.example.iot_project.classes.Set;
import com.example.iot_project.classes.Training;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.interfaces.SerialListenerInterface;
import com.github.mikephil.charting.data.Entry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class RepetitionsCounterActivity extends AppCompatActivity
        implements ServiceConnection, SerialListenerInterface {

    private enum Connected {False, Pending, True}
    private String leftDeviceAddress;
    private String rightDeviceAddress;  // not implemented
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean paused = false;
    private final String newline = TextUtil.newline_crlf;

    private ImageView setImageView;
    private TextView setNameText;
    private TextView cyclesLeftText;
    private TextView repetitionsLeftText;
    private TextView counterText;
    private TextView messageText;
    private Button buttonStart;
    private Button buttonReset;
    private Button buttonPause;
    private LinearLayout supporterGifLayout;
    private GifDrawable supporterGif;

    private int repetitions = 0;

    private Training training;
    private List<Set> sets;
    private int currentSetIndex;
    private int cyclesLeft;
    private int repetitionsLeft;
    private boolean isScheduled = false;
    private int position = 0;

    private ArrayList<Double> magnitudeArrayList = new ArrayList<>();
    private ArrayList<Double> movingAverageList = new ArrayList<>();
    private boolean started = false;
    private boolean isActivityResumed = false;
    private PyObject pyObject;

    private CountUpTimer timer;
    private TextView textViewTimer;
    private ImageView imageViewTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repetitions_counter);

        try {
            leftDeviceAddress = (String) Utilities.loadObject(
                    getFilesDir() + "/" + Constants.devicesDir + "left");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // chaquo
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        pyObject = Python.getInstance().getModule("repetitions_counter");

        // dynamic background
        AnimationDrawable animationDrawable =
                (AnimationDrawable) findViewById(R.id.repetitionsCounterActivityContainer).getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        // Binding to the serial service
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();

        training = (Training) intent.getSerializableExtra("training");
        isScheduled = intent.getBooleanExtra("scheduled", false);
        position = intent.getIntExtra("position", 0);

        buttonStart = findViewById(R.id.btnStartSet);
        buttonPause = findViewById(R.id.btnPauseSet);
        buttonReset = findViewById(R.id.btnRestartSet);
        setImageView = findViewById(R.id.imageViewCurrentSet);
        setNameText = findViewById(R.id.nameViewCurrentSet);
        cyclesLeftText = findViewById(R.id.textViewCyclesLeft);
        repetitionsLeftText = findViewById(R.id.textViewRepetitionsLeft);
        counterText = findViewById(R.id.textViewCounter);
        messageText = findViewById(R.id.textViewMessage);
        supporterGifLayout = findViewById(R.id.supporterGifLayout);
        supporterGif = (GifDrawable) ((GifImageView) findViewById(R.id.supporterGif)).getDrawable();
        textViewTimer = findViewById(R.id.textViewTimer);
        imageViewTimer = findViewById(R.id.stopwatchImageView);

        sets = training.getSets();
        setCurrentSetData(0, true);

        buttonStart.setVisibility(View.VISIBLE);
        buttonPause.setVisibility(View.INVISIBLE);
        buttonReset.setVisibility(View.INVISIBLE);
        counterText.setVisibility(View.INVISIBLE);
        messageText.setVisibility(View.INVISIBLE);
        supporterGifLayout.setVisibility(View.INVISIBLE);
        textViewTimer.setVisibility(View.INVISIBLE);
        imageViewTimer.setVisibility(View.INVISIBLE);

        timer = new CountUpTimer(Long.MAX_VALUE) {
            @Override
            public void onTick(int second, int millisecond) {
                int minute = (second % 3600) / 60;
                second = second % 60;
                millisecond = millisecond / 10;

                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", minute, second, millisecond);
                textViewTimer.setText(time);
            }

            @Override
            public void onFinish() {
            }
        };

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) {
                    if (connected != Connected.True) {
                        Toast.makeText(getBaseContext(), "Not connected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    send("START");
                    started = true;
                    buttonStart.setVisibility(View.INVISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonReset.setVisibility(View.VISIBLE);
                    counterText.setVisibility(View.VISIBLE);

                    if (paused) {
                        paused = false;
                        buttonPause.setVisibility(View.VISIBLE);
                    }
                    else {
                        startSet();
                    }
                }
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started) {
                    send("STOP");
                    started = false;
                }
                // TODO: insert in if
                paused = true;
                buttonStart.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);
                counterText.setVisibility(View.INVISIBLE);
                messageText.setVisibility(View.INVISIBLE);

            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                textViewTimer.setText(String.valueOf("00:00:00"));
                textViewTimer.setVisibility(View.INVISIBLE);
                imageViewTimer.setVisibility(View.INVISIBLE);
                started = false;
                paused = false;
                send("RESET");
                removeData();

                buttonStart.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);
                counterText.setVisibility(View.INVISIBLE);
                messageText.setVisibility(View.INVISIBLE);
                buttonReset.setVisibility(View.INVISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);
                repetitions = 0;
                setCurrentSetData(currentSetIndex, true);
            }
        });

        // CHEAT
        findViewById(R.id.btnCheat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repetitions++;
                repetitionsLeft--;
                repetitionsLeftText.setText(String.valueOf(repetitionsLeft));
                setRepetitionsText();
                requestFinishCycle();
                requestFinishSet();
                requestFinishTraining();
            }
        });

    }

    @Override
    public void onDestroy() {
        // Unbinding from the serial service
        unbindService(this);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (started) {
            started = false;    // our code
            send("STOP");
        }
        if (service != null && !isChangingConfigurations())
            service.detach();
        isActivityResumed = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
        isActivityResumed = true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isActivityResumed) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    private void cleanStr(String[] stringsArr) {
        for (int i = 0; i < stringsArr.length; i++) {
            stringsArr[i] = stringsArr[i].replaceAll(" ", "");
        }
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(leftDeviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(this, device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != Connected.True) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] data;
            data = (str + newline).getBytes();
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] message) {
        String msg = new String(message);

        if (msg.length() > 0) {
            // don't show CR as ^M if directly before LF
            String msg_to_save = msg;
            msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
            // check message length
            if (msg_to_save.length() > 1) {
                // split message string by ',' char
                String[] parts = msg_to_save.split(",");
                // function to trim blank spaces
                cleanStr(parts);

                try {
                    float time = Float.parseFloat(parts[0]);
                    // add value to array + call repetitions_counter
                    double magnitude = getMagnitude(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
                    magnitudeArrayList.add(Utilities.movingAverage(movingAverageList, magnitude, Constants.MOVING_AVG_WINDOW_SIZE));

                    double[] magnitudeArray = magnitudeArrayList.stream().mapToDouble(d -> d).toArray();

                    // pass magnitude data to python and receive the result
                    PyObject pyObject = this.pyObject.callAttr("main", (Object) magnitudeArray,
                            sets.get(currentSetIndex).getExercise().getModelHeight(),
                            sets.get(currentSetIndex).getExercise().getModelThreshold(),
                            sets.get(currentSetIndex).getExercise().getModelProminence());

                    int estimated =  pyObject.toJava(Integer.class);
                    if (estimated > repetitions) {
                        repetitions = estimated;
                        repetitionsLeft = sets.get(currentSetIndex).getRepetitions() - repetitions;

                        repetitionsLeftText.setText(String.valueOf(repetitionsLeft));
                        setRepetitionsText();

                        requestFinishCycle();
                        requestFinishSet();
                        requestFinishTraining();
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void status(String str) {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        started = false;
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            if (started)
                receive(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        started = false;
        disconnect();
    }

    private ArrayList<Entry> emptyDataValues() {
        return new ArrayList<>();
    }

    private void removeData() {
        magnitudeArrayList = new ArrayList<>();
        movingAverageList = new ArrayList<>();
    }

    private double getMagnitude(float x, float y, float z) {
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
    }

    private void setCurrentSetData(int index, boolean all) {
        if (all) {
            Set set = sets.get(index);
            setImageView.setImageResource(set.getImage());
            setNameText.setText(set.getName());
            cyclesLeft = set.getCycles();
            repetitionsLeft = set.getRepetitions();
            repetitions = 0;
        }
        cyclesLeftText.setText(String.valueOf(cyclesLeft));
        repetitionsLeftText.setText(String.valueOf(repetitionsLeft));
        setRepetitionsText();
    }

    private void startSet() {
        setCurrentSetData(0, true);
        messageText.setText(String.valueOf("Get ready..."));
        countDown(5, 0, "");
    }

    private boolean isFinishedSetCycle() {
        return repetitionsLeft <= 0;
    }

    private boolean isFinishedSet() {
        return repetitionsLeft <= 0 && cyclesLeft <= 0;
    }

    private boolean isFinishedTraining() {
        return repetitionsLeft <= 0 && cyclesLeft <= 0 && currentSetIndex >= sets.size();
    }

    private void requestFinishCycle() {
        if (isFinishedSetCycle()) {
            cyclesLeft--;
            cyclesLeftText.setText(String.valueOf(cyclesLeft));
            if (cyclesLeft > 0) {
                takeBreak(sets.get(currentSetIndex).getPauseBetweenCycles(), "GOOD!");
            }
        }
    }

    private void requestFinishSet() {
        if (isFinishedSet()) {
            currentSetIndex++;
            if (currentSetIndex < sets.size()) {
                setCurrentSetData(currentSetIndex, true);
                takeBreak(training.getPauseBetweenSets(), "EXCELLENT!");
            }
        }
    }

    private void requestFinishTraining() {
        if (isFinishedTraining()) {
            counterText.setVisibility(View.VISIBLE);
            textViewTimer.setVisibility(View.INVISIBLE);
            imageViewTimer.setVisibility(View.INVISIBLE);

            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    messageText.setVisibility(View.VISIBLE);
                    counterText.setText(String.valueOf("CONGRATS!"));
                    messageText.setTextSize(20);
                    messageText.setText(String.valueOf("See you next training!"));
                    supporterGifLayout.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, supporterGif.getDuration());
                }
            }.start();
        }
    }

    private void takeBreak(int pause, String msg) {
        timer.cancel();
        textViewTimer.setVisibility(View.INVISIBLE);
        imageViewTimer.setVisibility(View.INVISIBLE);
        messageText.setVisibility(View.VISIBLE);
        messageText.setTextSize(20);
        messageText.setText(String.valueOf("Take a break, breathe deep..."));
        messageText.setTextSize(40);
        magnitudeArrayList = new ArrayList<>();
        repetitionsLeft = sets.get(currentSetIndex).getRepetitions();
        repetitionsLeftText.setText(String.valueOf(repetitionsLeft));
        countDown(pause, 2, msg);
    }

    void countDown(int seconds, int wait, String msg) {
        buttonPause.setVisibility(View.INVISIBLE);
        buttonReset.setVisibility(View.INVISIBLE);

        new CountDownTimer((seconds + wait + 1) * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished > (seconds + 1) * 1000L) {
                    messageText.setVisibility(View.VISIBLE);
                    counterText.setText(String.valueOf(msg));
                }
                else if (millisUntilFinished > 1000) {
                    counterText.setText(String.valueOf(millisUntilFinished / 1000));
                }
                if (millisUntilFinished < 6000) {
                    messageText.setVisibility(View.VISIBLE);
                    messageText.setText(String.valueOf("Get ready..."));
                }
                if (millisUntilFinished < 1000) {
                    messageText.setVisibility(View.INVISIBLE);
                    counterText.setText(String.valueOf("GO!"));
                }
            }
            public void onFinish() {
                repetitions = 0;
                setRepetitionsText();
                textViewTimer.setVisibility(View.VISIBLE);
                imageViewTimer.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.VISIBLE);
                buttonReset.setVisibility(View.VISIBLE);
                send("START");
                started = true;
                timer.start();

            }
        }.start();
    }

    private void setRepetitionsText() {
        counterText.setText(String.valueOf("Reps: " + repetitions));
    }
}