package com.ywwynm.everythingdone.utils;

import android.content.Context;
import android.content.res.Resources;
import android.widget.EditText;

import com.ywwynm.everythingdone.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by ywwynm on 2015/8/27.
 * Helper for formatting datetime and getting information about that.
 */
public class DateTimeUtil {

    public static final String TAG = "EverythingDone$DateTimeUtil";

    public static String getGeneralDateTimeStr(Context context, long time) {
        return new DateTime(time).toString(getGeneralDateTimeFormatPattern(context));
    }

    public static String getGeneralDateTimeFormatPattern(Context context) {
        if (LocaleUtil.isChinese(context)) {
            return "yyyy年M月d日EEEE H:mm:ss";
        } else {
            return "H:mm:ss, MMM d, yyyy, EEEEEEEEE";
        }
    }

    public static String getDateTimeStr(int type, int time, Context context) {
        String typeStr = getTimeTypeStr(type, context);
        if (LocaleUtil.isChinese(context)) {
            return time + " " + typeStr;
        } else {
            if (time > 1) {
                typeStr += "s";
            }
            return time + " " + typeStr.toLowerCase();
        }
    }

    public static DateTimeFieldType getJodaType(int type) {
        switch (type) {
            case Calendar.MINUTE:
                return DateTimeFieldType.minuteOfHour();
            case Calendar.HOUR_OF_DAY:
                return DateTimeFieldType.hourOfDay();
            case Calendar.DATE:
                return DateTimeFieldType.dayOfMonth();
            case Calendar.WEEK_OF_YEAR:
                return DateTimeFieldType.weekOfWeekyear();
            case Calendar.MONTH:
                return DateTimeFieldType.monthOfYear();
            case Calendar.YEAR:
                return DateTimeFieldType.year();
            default:
                return DateTimeFieldType.era();
        }
    }

    public static String getTimeTypeStr(int type, Context context) {
        switch (type) {
            case Calendar.MINUTE:
                return context.getString(R.string.minute);
            case Calendar.HOUR_OF_DAY:
                return context.getString(R.string.hours);
            case Calendar.DATE:
                return context.getString(R.string.days);
            case Calendar.WEEK_OF_YEAR:
                return context.getString(R.string.weeks);
            case Calendar.MONTH:
                return context.getString(R.string.months);
            case Calendar.YEAR:
                return context.getString(R.string.year);
            default:
                return "";
        }
    }

    public static long getActualTimeAfterSomeTime(int[] reminderAfterTime) {
        if (reminderAfterTime.length != 2) return 0;
        return getActualTimeAfterSomeTime(reminderAfterTime[0], reminderAfterTime[1]);
    }

    public static long getActualTimeAfterSomeTime(int type, int afterTime) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(type, afterTime);
        return calendar.getTimeInMillis();
    }

    /**
     * Used to display {@link com.ywwynm.everythingdone.model.Reminder.notifyTime}
     * of a Reminder which belongs to a {@link com.ywwynm.everythingdone.model.Thing}
     * object with type {@link com.ywwynm.everythingdone.model.Thing.GOAL}.
     *
     * Using countdown to stress importance of a GOAL.
     *
     * @return A string with type of "after some days" according to {@param time}.
     */
    public static String getDateTimeStrGoal(long time, Context context) {
        int days = calculateTimeGap(System.currentTimeMillis(), time, Calendar.DATE);
        return getDateTimeStrAfter(Calendar.DATE, days, context);
    }

    /**
     * Used to display {@link com.ywwynm.everythingdone.model.Reminder.notifyTime}
     * of a Reminder which belongs to a {@link com.ywwynm.everythingdone.model.Thing}
     * object with type {@link com.ywwynm.everythingdone.model.Thing.REMINDER}.
     *
     * @return A string with type of "after some time" according to {@param time}.
     *         For example, "after 15 minutes" or "after 1 day".
     */
    public static String getDateTimeStrAfter(int type, int time, Context context) {
        if (time == 0) {
            return getThisTStr(type, context);
        }
        String str = getDateTimeStr(type, time, context);
        String after = context.getString(R.string.after);
        if (LocaleUtil.isChinese(context)) {
            return str + after;
        } else {
            return after + " " + str;
        }
    }

    public static String getDateTimeStrAt(long time, Context context, boolean timePeriod) {
        DateTime dt = new DateTime(time);
        return getDateTimeStrAt(dt, context, timePeriod);
    }

    /**
     * Used to display {@link com.ywwynm.everythingdone.model.Reminder.notifyTime}
     * of a Reminder which belongs to a {@link com.ywwynm.everythingdone.model.Thing}
     * object with type {@link com.ywwynm.everythingdone.model.Thing.REMINDER} in detailed way.
     *
     * @param dt A {@link DateTime} object which has called
     *                 {@code DateTime#withMillis(long)}
     *                 to set the correct time of
     *                 {@link com.ywwynm.everythingdone.model.Reminder.notifyTime}.
     *
     * @param timePeriod Whether the returned string should contain time period
     *                   information such as "in the morning", "at night" and so on.
     *                   Used in non-Chinese contexts.
     *
     * @return A string describing time in a detailed way. For example, will be
     *         "on Jan 29, 1995, 16:40", "yesterday, 2:33", "星期六清晨5:55" and so on.
     */
    public static String getDateTimeStrAt(DateTime dt, Context context, boolean timePeriod) {
        DateTime cur = new DateTime();
        int year = dt.getYear();
        int curYear = cur.getYear();
        int month = dt.getMonthOfYear();
        int day = dt.getDayOfMonth();
        int dayOfWeek = dt.getDayOfWeek();
        dayOfWeek = dayOfWeek == 7 ? 1 : dayOfWeek + 1;
        int curDayOfWeek = cur.getDayOfWeek();
        curDayOfWeek = curDayOfWeek == 7 ? 1 : curDayOfWeek + 1;
        int hour = dt.getHourOfDay();
        int minute = dt.getMinuteOfHour();

        Resources res = context.getResources();
        Date date = dt.toDate();
        StringBuilder sb = new StringBuilder();
        boolean isChinese = LocaleUtil.isChinese(context);

        int days = calculateTimeGap(cur.getMillis(), dt.getMillis(), Calendar.DATE);
        if (days < 0) {
            if (days == -1) {
                sb.append(res.getString(R.string.yesterday));
            } else if (days >= -getEarlyWeekLimitDays(curDayOfWeek)) {
                if (!isChinese) {
                    sb.append("on ");
                }
                sb.append(res.getStringArray(R.array.day_of_week)[dayOfWeek - 1]);
            } else {
                appendYearMonthDayStr(year, month, day, curYear, date, context, sb, isChinese);
            }
        } else if (days == 0) {
            sb.append(res.getString(R.string.today));
        } else {
            if (days == 1) {
                sb.append(res.getString(R.string.tomorrow));
            } else if (days <= getLateWeekLimitDays(curDayOfWeek)) {
                if (!isChinese) {
                    sb.append("on ");
                }
                sb.append(res.getStringArray(R.array.day_of_week)[dayOfWeek - 1]);
            } else {
                appendYearMonthDayStr(year, month, day, curYear, date, context, sb, isChinese);
            }
        }
        if (isChinese) {
            sb.append(getTimePeriodStr(hour, res)).append(" ");
            appendHourMinute(hour, minute, sb);
        } else {
            sb.append(", ");
            appendHourMinute(hour, minute, sb);
            if (timePeriod) {
                sb.append(" ").append(getTimePeriodStr(hour, res));
            }
        }
        return sb.toString();
    }

    public static void limitHourForEditText(EditText et) {
        String hourStr = et.getText().toString();
        if (!hourStr.isEmpty()) {
            int hour = Integer.parseInt(hourStr);
            if (hour >= 24) {
                et.setText("23");
            }
        }
    }

    public static void formatLimitMinuteForEditText(EditText et) {
        String minuteStr = et.getText().toString();
        if (minuteStr.length() == 1) {
            et.setText("0" + minuteStr);
        } else if (!minuteStr.isEmpty()) {
            int minute = Integer.parseInt(minuteStr);
            if (minute > 59) {
                et.setText("59");
            }
        }
    }

    public static String getDateTimeStrRec(Context context, int type, String detail) {
        if (type == Calendar.DATE) {
            return getDateTimeStrRecTimeOfDay(context, detail);
        } else if (type == Calendar.WEEK_OF_YEAR) {
            return getDateTimeStrRecDayOfWeek(context, detail);
        } else if (type == Calendar.MONTH) {
            return getDateTimeStrRecDayOfMonth(context, detail);
        } else if (type == Calendar.YEAR) {
            return getDateTimeStrRecMonthOfYear(context, detail);
        }
        return null;
    }

    private static String getDateTimeStrRecTimeOfDay(Context context, String detail) {
        StringBuilder sb = new StringBuilder();
        String every = context.getString(R.string.every);
        String day = context.getString(R.string.days); // 天
        boolean isChinese = LocaleUtil.isChinese(context);
        if (isChinese) {
            sb.append(every).append(day).append(" ");
        } else {
            sb.append("at ");
        }
        String[] times = detail.split(",");
        for (String time : times) {
            sb.append(time).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        if (!isChinese) {
            sb.append(every).append(" ").append(day);
        }
        return sb.toString();
    }

    private static String getDateTimeStrRecDayOfWeek(Context context, String detail) {
        StringBuilder sb = new StringBuilder();
        String[] dateTimes = detail.split(" ");
        String[] days = dateTimes[0].split(",");
        String[] times = dateTimes[1].split(":");
        String every = context.getString(R.string.every);
        String[] dayOfWeek = context.getResources().getStringArray(R.array.day_of_week);
        boolean isChinese = LocaleUtil.isChinese(context);
        if (isChinese) {
            sb.append(every);
            sb.append(dayOfWeek[0].substring(0, 1));
            for (String day : days) {
                sb.append(dayOfWeek[Integer.parseInt(day)].substring(1, 2)).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append(getTimePeriodStr(Integer.parseInt(times[0]), context.getResources()))
                    .append(" ").append(dateTimes[1]);
        } else {
            sb.append("at ");
            sb.append(dateTimes[1]).append(every).append(" ");
            for (String day : days) {
                sb.append(dayOfWeek[Integer.parseInt(day)]).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String getDateTimeStrRecDayOfMonth(Context context, String detail) {
        StringBuilder sb = new StringBuilder();
        String[] dateTimes = detail.split(" ");
        String[] days = dateTimes[0].split(",");
        String[] times = dateTimes[1].split(":");
        String every = context.getString(R.string.every);
        String monthStr = context.getString(R.string.months);
        boolean isChinese = LocaleUtil.isChinese(context);
        if (isChinese) { // 每个月 1号, 6号, 16号, 26号早晨 6:30
            sb.append(every).append(monthStr).append(" ");
            String monthDay = context.getString(R.string.month_day);
            for (String day : days) {
                if ("27".equals(day)) {
                    sb.append(context.getString(R.string.end_of_month)).append(", ");
                } else {
                    sb.append(Integer.parseInt(day) + 1).append(monthDay).append(", ");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append(getTimePeriodStr(Integer.parseInt(times[0]), context.getResources()))
                    .append(" ").append(dateTimes[1]);
        } else { // at 6:30 on the 1st, 6th, 16th, 26th day of every month
            sb.append("at ");
            sb.append(dateTimes[1]).append(" on the ");
            for (String day : days) {
                if ("27".equals(day)) {
                    sb.append("last, ");
                } else {
                    sb.append(getDayOfMonthStrInEnglish(day)).append(", ");
                }
            }
            sb.deleteCharAt(sb.length() - 2);
            sb.append("day of").append(every).append(" ").append(monthStr);
        }
        return sb.toString();
    }

    private static String getDateTimeStrRecMonthOfYear(Context context, String detail) {
        StringBuilder sb = new StringBuilder();
        String[] dateTimes = detail.split(" ");
        String[] months = dateTimes[0].split(",");
        String day = dateTimes[1];
        String[] times = dateTimes[2].split(":");
        String every = context.getString(R.string.every);
        String yearStr = context.getString(R.string.year).toLowerCase();
        String[] monthOfYear = context.getResources().getStringArray(R.array.month_of_year);
        boolean isChinese = LocaleUtil.isChinese(context);
        if (isChinese) { // 每年六月, 十二月月末傍晚 18:00
            sb.append(every).append(yearStr);
            for (String month : months) {
                sb.append(monthOfYear[Integer.parseInt(month)]).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            if ("28".equals(day)) {
                sb.append(context.getString(R.string.end_of_month));
            } else {
                sb.append(" ").append(Integer.parseInt(day)).append(context.getString(R.string.month_day));
            }
            sb.append(getTimePeriodStr(Integer.parseInt(times[0]), context.getResources()))
                    .append(" ").append(dateTimes[2]);
        } else { // at 18:00 on the last day of June, December in every year
            sb.append("at ");
            sb.append(dateTimes[2]).append(" on the ");
            if ("28".equals(day)) {
                sb.append("last");
            } else {
                sb.append(getDayOfMonthStrInEnglish(day));
            }
            sb.append(" day of ");
            for (String month : months) {
                sb.append(monthOfYear[Integer.parseInt(month)]).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" in").append(every).append(" ").append(yearStr);
        }
        return sb.toString();
    }

    private static String getDayOfMonthStrInEnglish(String dayStr) {
        int day = Integer.parseInt(dayStr);
        String postfix;
        if (day % 10 == 0) {
            postfix = "st";
        } else if (day % 10 == 1) {
            postfix = "nd";
        } else if (day % 11 == 2) {
            postfix = "rd";
        } else {
            postfix = "th";
        }
        return (day + 1) + postfix;
    }

    // todo: add annotations for methods below.
    private static void appendYearMonthDayStr(int year, int month, int day, int curYear,
                                             Date date, Context context, StringBuilder sb, boolean isChinese) {
        Resources res = context.getResources();
        if (isChinese) {
            if (year != curYear) {
                sb.append(year)
                        .append(res.getString(R.string.year));
            }
            sb.append(month)
                    .append(res.getString(R.string.month))
                    .append(day)
                    .append(res.getString(R.string.day));
        } else {
            sb.append("on ");
            SimpleDateFormat sdf;
            if (year != curYear) {
                sdf = new SimpleDateFormat("MMM d, yyyy");
            } else {
                sdf = new SimpleDateFormat("MMM d");
            }
            sb.append(sdf.format(date));
        }
    }

    private static void appendHourMinute(int hour, int minute, StringBuilder sb) {
        sb.append(hour).append(":");
        if (minute < 10) {
            sb.append(0);
        }
        sb.append(minute);
    }

    private static int getEarlyWeekLimitDays(int dayOfWeek) {
        return dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;
    }

    private static int getLateWeekLimitDays(int dayOfWeek) {
        return dayOfWeek == Calendar.SUNDAY ? 0 : 8 - dayOfWeek;
    }

    public static String getTimePeriodStr(int hour, Resources res) {
        String[] periods = res.getStringArray(R.array.time_period);
        int[] limits = { 6, 8, 12, 13, 17, 19, 22 };
        for (int i = 0; i < limits.length; i++) {
            if (hour < limits[i]) {
                return periods[i];
            }
        }
        return periods[7];
    }

    public static int getTimeTypeLimit(int y, int m, int index) {
        if (index == 1) {
            return 12;
        } else if (index == 3) {
            return 23;
        } else if (index == 4) {
            return 59;
        } else if (index == 2) {
            return getDaysOfMonth(y, m);
        } else return Integer.MAX_VALUE;
    }

    public static String getDurationBriefStr(long time) {
        float second = time / 1000f;
        if (second < 1) {
            return "< 1s";
        } else if (second < 3600) {
            return new SimpleDateFormat("mm:ss").format(new Date(time));
        } else return new SimpleDateFormat("HH:mm:ss").format(new Date(time));
    }

    public static String getTimeLengthStr(long time, Context context) {
        boolean isChinese = LocaleUtil.isChinese(context);
        final String BLK = isChinese ? " " : "";

        long second = time / 1000;
        String secStr = context.getString(R.string.statistic_second);
        String minStr = context.getString(R.string.statistic_minute);
        String hourStr = context.getString(R.string.statistic_hour);
        String dayStr = context.getString(R.string.statistic_day);
        String yearStr = context.getString(R.string.statistic_year);
        if (second < 1) {
            return "< 1" + BLK + secStr;
        } else if (second < 60) {
            return second + BLK + secStr;
        } else if (second < 3600) {
            long min = second / 60;
            long sec = second % 60;
            if (sec == 0) {
                return min + BLK + minStr;
            } else {
                if (isChinese) {
                    minStr = minStr.substring(0, minStr.length() - 1);
                }
                return min + BLK + minStr + " " + sec + BLK + secStr;
            }
        } else if (second < 86400) {
            long hour = second / 3600;
            long min = (second % 3600) / 60;
            if (min == 0) {
                return hour + BLK + hourStr;
            } else {
                return hour + BLK + hourStr + " " + min + BLK + minStr;
            }
        } else if (second < 86400 * 365) {
            long day  =  second / 86400;
            long hour = (second % 86400) / 3600;
            long min  = ((second % 86400) % 3600) / 60;
            if (hour == 0) {
                return day + BLK + dayStr;
            } else if (min == 0) {
                return day + BLK + dayStr + " " + hour + BLK + hourStr;
            } else {
                return day + BLK + dayStr + " "
                        + hour + BLK + hourStr + " "
                        + min + BLK + minStr;
            }
        } else {
            long year =   second / 31536000;
            long day  =  (second % 31536000) / 86400;
            long hour = ((second % 31536000) % 86400) / 3600;
            if (hour > 12) day++;
            if (day == 0) {
                return year + BLK + yearStr;
            } else {
                return year + BLK + year + " " + day + BLK + dayStr;
            }
        }
    }

    public static String getTimeLengthStrOnlyDay(long time, Context context) {
        long day = time / 86400000L;
        long hour = (time % 86400000) / 3600;
        if (hour > 12) day++;
        String dayStr = context.getString(R.string.days);
        if (day == 0) {
            return "< 1 " + dayStr;
        } else if (day == 1) {
            return day + " " + dayStr;
        } else {
            return day + " " + dayStr + (LocaleUtil.isChinese(context) ? "" : "s");
        }
    }

    public static long getHabitReminderTime(int type, long curHrTime, int vary) {
        if (type == Calendar.DATE) {
            return curHrTime + vary * 86400000L;
        } else if (type == Calendar.WEEK_OF_YEAR) {
            return curHrTime + vary * 604800000L;
        }
        DateTime dt = new DateTime(curHrTime);
        int year = dt.getYear();
        int month = dt.getMonthOfYear();
        int day = dt.getDayOfMonth();
        if (type == Calendar.MONTH) {
            int days = getDaysOfMonth(year, month);
            dt = dt.plusMonths(vary);
            if (day == days) {
                year = dt.getYear();
                month = dt.getMonthOfYear();
                dt = dt.withDayOfMonth(getDaysOfMonth(year, month));
            }
            return dt.getMillis();
        } else if (type == Calendar.YEAR) {
            int days = getDaysOfMonth(year, month);
            dt = dt.plusYears(vary);
            if (day == days) {
                dt = dt.withDayOfMonth(getDaysOfMonth(year + vary, month));
            }
            return dt.getMillis();
        }
        return 0;
    }

    public static int getDaysOfMonth(int y, int m) {
        int[] days = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) {
            days[1] = 29;
        }
        return days[m - 1];
    }

    public static String getThisTStr(int type, Context context) {
        if (type == Calendar.DATE) {
            return context.getString(R.string.today);
        } else if (type == Calendar.WEEK_OF_YEAR) {
            return context.getString(R.string.this_week);
        } else if (type == Calendar.MONTH) {
            return context.getString(R.string.this_month);
        } else if (type == Calendar.YEAR) {
            return context.getString(R.string.this_year);
        }
        return "";
    }

    public static int calculateTimeGap(long start, long end, int type) {
        DateTime sDt = new DateTime(start).withTime(0, 0, 0, 0);
        DateTime eDt = new DateTime(end).withTime(0, 0, 0, 0);

        if (type == Calendar.DATE) {
            return Days.daysBetween(sDt, eDt).getDays();
        } else if (type == Calendar.WEEK_OF_YEAR) {
            sDt = sDt.withDayOfWeek(1);
            eDt = eDt.withDayOfWeek(1);
            return Weeks.weeksBetween(sDt, eDt).getWeeks();
        } else if (type == Calendar.MONTH) {
            sDt = sDt.withDayOfMonth(1);
            eDt = eDt.withDayOfMonth(1);
            return Months.monthsBetween(sDt, eDt).getMonths();
        } else if (type == Calendar.YEAR) {
            return eDt.getYear() - sDt.getYear();
        }
        return 0;
    }

}