package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.iot_project.classes.Utilities;
import com.example.iot_project.interfaces.SerialListenerInterface;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ExerciseTrainModelActivity extends AppCompatActivity
        implements ServiceConnection, SerialListenerInterface {

    private enum Connected {False, Pending, True}
    private String leftDeviceAddress;
    private String rightDeviceAddress;  // not implemented
    private SerialService service;
    private TextView receiveText;
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean pendingNewline = false;
    private TextInputEditText actualRepetitions;

    private LineChart mpLineChart;
    private LineDataSet lineDataSetMagnitude;
    private LineData magnitudeData;
    private ArrayList<Double> magnitudeArrayList = new ArrayList<>();
    private ArrayList<Double> movingAverageList = new ArrayList<>();
    private boolean started = false;
    private boolean isActivityResumed = false;
    private PyObject pyObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_train_model);

//        magnitudeArrayList.add((double) 0);  // TODO: remove this filler

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

        pyObject = Python.getInstance().getModule("calibration");

        // Binding to the serial service
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);


        receiveText = findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.light_gray, null)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        mpLineChart = findViewById(R.id.line_chart);

        lineDataSetMagnitude = new LineDataSet(emptyDataValues(), "Magnitude");

        lineDataSetMagnitude.setColor(Color.rgb(0, 200, 255));
        lineDataSetMagnitude.setCircleColor(Color.rgb(255, 180, 10));

        ArrayList<ILineDataSet> magnitudeSets = new ArrayList<>();
        magnitudeSets.add(lineDataSetMagnitude);
        magnitudeData = new LineData(magnitudeSets);

        mpLineChart.setData(magnitudeData);

        mpLineChart.getDescription().setEnabled(false);
        mpLineChart.getLegend().setEnabled(true);
        mpLineChart.invalidate();

        actualRepetitions = findViewById(R.id.modelRepetitionsNumber);

        Button buttonStart = findViewById(R.id.btnStartCalibration);
        Button buttonPause = findViewById(R.id.btnPauseCalibration);
        Button buttonReset = findViewById(R.id.btnResetCalibration);
        Button buttonSave = findViewById(R.id.btnSaveCalibration);

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
                    Toast.makeText(getBaseContext(), "Starting...", Toast.LENGTH_SHORT).show();
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
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = false;
                send("RESET");
                removeData();
                refreshGraph();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        actualRepetitions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputLayout nameTextContainer = findViewById(R.id.repetitionsTextContainerModel);
                nameTextContainer.setDefaultHintTextColor(
                        ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.light_gray)));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        actualRepetitions.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Clear focus here
                    actualRepetitions.clearFocus();
                }
                return false;
            }
        });

        actualRepetitions.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    started = false;
                    send("STOP");
                }
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
            String msg;
            byte[] data;
            msg = str;
            String newline = TextUtil.newline_crlf;
            data = (str + newline).getBytes();
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(Color.rgb(15, 70, 190)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
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

                    // magnitude chart + add value to array
                    double magnitude = getMagnitude(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
                    magnitudeData.addEntry(new Entry(time,
                            (float) Utilities.movingAverage(movingAverageList, magnitude, Constants.MOVING_AVG_WINDOW_SIZE)), 0);
                    magnitudeArrayList.add(Utilities.movingAverage(movingAverageList, magnitude, Constants.MOVING_AVG_WINDOW_SIZE));
                    refreshGraph();

                } catch (IllegalArgumentException ignored) {
                }
            }

            msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
            // send msg to function that saves it to csv
            // special handling if CR and LF come in separate fragments
            if (pendingNewline && msg.charAt(0) == '\n') {
                Editable edt = receiveText.getEditableText();
                if (edt != null && edt.length() > 1)
                    edt.replace(edt.length() - 2, edt.length(), "");
            }
            pendingNewline = msg.charAt(msg.length() - 1) == '\r';
        }
        receiveText.append(TextUtil.toCaretString(msg, true));
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(Color.rgb(10, 110, 45)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
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

    private void saveData() {
        if (magnitudeArrayList.isEmpty()){
            Toast.makeText(getBaseContext(),
                    "No data to save, check connection, then start and perform the exercise",
                    Toast.LENGTH_LONG).show();
        }
        else if (magnitudeArrayList.size() < Constants.MIN_SAMPLES) {
            Toast.makeText(getBaseContext(),
                    "Not enough data, please continue",
                    Toast.LENGTH_LONG).show();
        }
        else if (Objects.requireNonNull(actualRepetitions.getText()).toString().isEmpty()) {
            TextInputLayout nameTextContainer = findViewById(R.id.repetitionsTextContainerModel);
            nameTextContainer.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
        }
        else {
            send("RESET");
            double[] magnitudeArray = magnitudeArrayList.stream().mapToDouble(d -> d).toArray();
            // pass magnitude data to python and receive the result
            PyObject pyObject = this.pyObject.callAttr("main", (Object) magnitudeArray,
                    Integer.parseInt(Objects.requireNonNull(actualRepetitions.getText()).toString()));
            Float[] modelParameters = pyObject.toJava(Float[].class);
            // return the result
            Intent intent = new Intent();
            intent.putExtra("height", modelParameters[0]);
            intent.putExtra("threshold", modelParameters[1]);
            intent.putExtra("prominence", modelParameters[2]);
            intent.putExtra("calibrated", true);
            setResult(Activity.RESULT_OK, intent);
            Toast.makeText(getBaseContext(), "Result: "+ modelParameters[0] + ", " +
                    modelParameters[1] + ", " + modelParameters[2], Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void refreshGraph() {
        lineDataSetMagnitude.notifyDataSetChanged();
        mpLineChart.notifyDataSetChanged(); // let the chart know it's data changed
        mpLineChart.invalidate(); // refresh
    }

    private void removeData() {
        ILineDataSet set = magnitudeData.getDataSetByIndex(0);
        while (set.removeLast()) {
        }
        magnitudeArrayList = new ArrayList<>();
        movingAverageList = new ArrayList<>();
    }

    private double getMagnitude(float x, float y, float z) {
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
    }
}