package com.m.berber.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrayerTimeUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Checks if the given date is a Friday (Cuma).
     */
    public static boolean isFriday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
            }
        } catch (ParseException ignored) {}
        return false;
    }

    /**
     * Checks whether a time string "HH:mm" falls within prayer restriction hours.
     * Returns null if available, or a descriptive reason if restricted (e.g. "Cuma Saati", "Öğle Namazı Vakti").
     */
    public static String getPrayerRestrictionReason(String dateStr, String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;

        int minutes = parseTimeToMinutes(timeStr);
        if (minutes < 0) return null;

        // Friday Prayer Restriction (Cuma Saati: 12:00 - 14:00)
        if (isFriday(dateStr)) {
            if (minutes >= parseTimeToMinutes("12:00") && minutes <= parseTimeToMinutes("14:00")) {
                return "Cuma Namazı Saati (Randevu Alınamaz)";
            }
        }

        // Daily Prayer Time Restrictions
        // Öğle Namazı Vakti (12:15 - 13:30)
        if (minutes >= parseTimeToMinutes("12:15") && minutes <= parseTimeToMinutes("13:30")) {
            return "Öğle Namazı Vakti (Randevu Alınamaz)";
        }

        // İkindi Namazı Vakti (15:45 - 16:45)
        if (minutes >= parseTimeToMinutes("15:45") && minutes <= parseTimeToMinutes("16:45")) {
            return "İkindi Namazı Vakti (Randevu Alınamaz)";
        }

        // Akşam Namazı Vakti (19:15 - 20:15)
        if (minutes >= parseTimeToMinutes("19:15") && minutes <= parseTimeToMinutes("20:15")) {
            return "Akşam Namazı Vakti (Randevu Alınamaz)";
        }

        return null;
    }

    public static boolean isPrayerTime(String dateStr, String timeStr) {
        return getPrayerRestrictionReason(dateStr, timeStr) != null;
    }

    private static int parseTimeToMinutes(String timeStr) {
        try {
            String[] parts = timeStr.trim().split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return h * 60 + m;
        } catch (Exception ignored) {}
        return -1;
    }
}