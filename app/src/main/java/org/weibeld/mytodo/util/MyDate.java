package org.weibeld.mytodo.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A date that can be represented as a "d/m/yy" string or as a UNIX timestamp in milliseconds.
 */
public class MyDate {

    private long mTimestamp;
    private SimpleDateFormat mFormatDateShort = new SimpleDateFormat("d/M/yy", Locale.UK);
    private SimpleDateFormat mFormatDateLong = new SimpleDateFormat("d/M/yyyy", Locale.UK);
    private SimpleDateFormat mFormatDateDay = new SimpleDateFormat("d/M/y (EEEE)", Locale.UK);
    private SimpleDateFormat mFormatDateDayTime = new SimpleDateFormat("d/M/y (EEEE) HH:mm", Locale.UK);

    // (y, m, d) --> timestamp
    /**
     * Create a MyDate object by specifying year, month, and day.
     * @param year e.g. 2017
     * @param month 0-11
     * @param day 1-31
     */
    public MyDate(int year, int month, int day) {
        mTimestamp = new GregorianCalendar(year, month, day).getTimeInMillis();
    }

    // text --> timestamp
    /**
     * Create a MyDate object from a string that contains a date in "d/m/yy" format.
     * @param str A string containing a "d/m/yy" date as a substring.
     */
    public MyDate(String str) {
        for (int i = 0; i < str.length()-1; i++) {
            Date date = mFormatDateShort.parse(str, new ParsePosition(i));
            if (date != null) {
                mTimestamp = date.getTime();
                return;
            }
        }
        (new Exception("Invalid date string: " + str)).printStackTrace();
    }

    /**
     * Create a MyDate object from a UNIX timestamp in milliseconds.
     * @param timestamp UNIX timestamp in milliseconds.
     */
    public MyDate(long timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * Create a MyDate object of the current date and time and default time zone.
     */
    public MyDate() {
        mTimestamp = GregorianCalendar.getInstance().getTimeInMillis();
    }

    /**
     * Return a string of the date represented by this object in "d/m/y" format, e.g. "13/2/17".
     */
    public String formatDateShort() {
        return mFormatDateShort.format(mTimestamp);
    }

    /**
     * Return a string of the date represented by this object in "d/m/yy" format, e.g. "13/2/2017".
     */
    public String formatDateLong() {
        return mFormatDateLong.format(mTimestamp);
    }

    /**
     * Return a string of the date represented by this object in "d/m/y (weekday)" format,
     * e.g. "13/2/2017 (Monday)".
     */
    public String formatDateDay() {
        return mFormatDateDay.format(mTimestamp);
    }

    /**
     * Return a string of the date represented by this object in "d/m/y (weekday) H:M" format,
     * e.g. "13/2/2017 (Monday) 20:12".
     */
    public String formatDateDayTime() {
        return mFormatDateDayTime.format(mTimestamp);
    }

    /**
     * @return The UNIX timestamp in milliseconds of the date represented by this object.
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * @return the year of this date, e.g. 2017
     */
    public int getYear() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
        return cal.get(Calendar.YEAR);
    }

    /**
     * @return the month of this date starting from 0 (i.e. 0-11)
     */
    public int getMonth() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
        return cal.get(Calendar.MONTH);
    }

    /**
     * @return the day of the month of this date (1-31)
     */
    public int getDay() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
            return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Test if another MyDate object represents the same point in time as this object.
     * @param obj Another MyDate object.
     */
    @Override
    public boolean equals(Object obj) {
        return ((MyDate) obj).getTimestamp() == mTimestamp;
    }
}
