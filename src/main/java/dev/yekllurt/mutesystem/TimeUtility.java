package dev.yekllurt.mutesystem;

import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;

import java.util.Calendar;
import java.util.Date;

public class TimeUtility {

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Calendar getCurrentCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentDate());
        return calendar;
    }

    public static String replaceTimeVariables(LanguageSystem languageSystem, Calendar calendar, String text) {
        text = text.replaceAll("%day_of_month%", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))
                .replaceAll("%month%", String.valueOf(calendar.get(Calendar.MONTH)))
                .replaceAll("%year%", String.valueOf(calendar.get(Calendar.YEAR)));
        text = text.replaceAll("%second%", String.valueOf(calendar.get(Calendar.SECOND)))
                .replaceAll("%minute%", String.valueOf(calendar.get(Calendar.MINUTE)))
                .replaceAll("%hour_12_hour_clock%", String.valueOf(calendar.get(Calendar.HOUR)))
                .replaceAll("%am_pm%",
                        String.valueOf(
                                calendar.get(Calendar.AM_PM) == Calendar.AM ? languageSystem.getMessage("time.am", true)
                                        : languageSystem.getMessage("time.pm", true)))
                .replaceAll("%hour_24_hour_clock%", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
        return text;
    }

}
