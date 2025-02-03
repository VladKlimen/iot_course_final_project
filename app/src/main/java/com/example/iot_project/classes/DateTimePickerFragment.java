package com.example.iot_project.classes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Calendar;

public class DateTimePickerFragment extends DialogFragment {

    public interface OnDateTimeSetListener {
        void onDateTimeSet(int year, int month, int day, int hour, int minute) throws IOException;
    }

    private OnDateTimeSetListener listener;
    private int year, month, day, hour, minute;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDateTimeSetListener) {
            listener = (OnDateTimeSetListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDateTimeSetListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date and time as the default values
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, year, month, day);

        // Set the DatePicker bounds (Optional)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);  // Disable past dates

        // Customize the Positive (Schedule) and Negative (Cancel) buttons
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Next", datePickerDialog);
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);

        return datePickerDialog;
    }

    private final DatePickerDialog.OnDateSetListener dateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    DateTimePickerFragment.this.year = year;
                    DateTimePickerFragment.this.month = month;
                    DateTimePickerFragment.this.day = day;
                    // the user has set the date, now set the time
                    showTimePicker();
                }
            };

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), timeSetListener, hour, minute, true);

        // Customize the Positive (Schedule) and Negative (Cancel) buttons
        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Schedule", timePickerDialog);
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);

        timePickerDialog.show();
    }


    private final TimePickerDialog.OnTimeSetListener timeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hour, int minute) {
                    DateTimePickerFragment.this.hour = hour;
                    DateTimePickerFragment.this.minute = minute;
                    try {
                        listener.onDateTimeSet(year, month, day, hour, minute);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
}


