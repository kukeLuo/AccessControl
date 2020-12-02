package com.brc.acctrl.utils;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

public class TimePickerUtil {
    public static void showTimerPickerDialog(Context context, int hourOfDay, int minute, final OnTimerPickerListener onTimerPickerListener) {
        TimePickerDialog dialog = new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (onTimerPickerListener != null) {
                    onTimerPickerListener.onConfirm(hourOfDay, minute);
                }
            }
        }, hourOfDay, minute, true);

//        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                if (onTimerPickerListener != null) {
//                    onTimerPickerListener.onCancel();
//                }
//            }
//        });

        dialog.setTitle("请选择时间");
        dialog.show();
    }

    public interface OnTimerPickerListener {
        void onConfirm(int hourOfDay, int minute);

//        void onCancel();
    }
}
