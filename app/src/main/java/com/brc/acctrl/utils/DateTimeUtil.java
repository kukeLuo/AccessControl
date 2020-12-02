package com.brc.acctrl.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm");
    public static String curTimeFormat() {
        return timeFormat.format(new Date());
    }
    private static SimpleDateFormat meetingTimeFormat = new SimpleDateFormat("HH:mm");

    private static SimpleDateFormat accessTimeFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
    public static String accessTimeFormat(long timeMills) {
        return accessTimeFormat.format(new Date(timeMills));
    }

    public static String formatMeetingTime(long timeMills) {
        return meetingTimeFormat.format(new Date(timeMills));
    }
}
