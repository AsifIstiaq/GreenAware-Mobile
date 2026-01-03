package com.example.greenaware_mobile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DeadlineUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static boolean isDeadlineTomorrow(String deadlineStr) {
        try {
            Date deadlineDate = dateFormat.parse(deadlineStr);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar deadline = Calendar.getInstance();
            deadline.setTime(deadlineDate);
            deadline.set(Calendar.HOUR_OF_DAY, 0);
            deadline.set(Calendar.MINUTE, 0);
            deadline.set(Calendar.SECOND, 0);
            deadline.set(Calendar.MILLISECOND, 0);

            long diffMillis = deadline.getTimeInMillis() - today.getTimeInMillis();
            long diffDays = diffMillis / (24 * 60 * 60 * 1000);

            return diffDays == 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
