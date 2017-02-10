package org.weibeld.mytodo.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A date that can be represented as a "d/m/yy" string or as a UNIX timestamp in milliseconds.
 */
public class MyDate {

    private long mTimestamp;
    private SimpleDateFormat mFormatShort = new SimpleDateFormat("d/M/yy");
    private SimpleDateFormat mFormatLong = new SimpleDateFormat("d/M/y, H:m (z)");

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
            Date date = mFormatShort.parse(str, new ParsePosition(i));
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

    // timestamp --> text
    @Override
    public String toString() {
        return mFormatShort.format(mTimestamp);
    }

    public String formatShort() {
        return this.toString();
    }

    public String formatLong() {
        return mFormatLong.format(mTimestamp);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * @return the year, e.g. 2017
     */
    public int getYear() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
        return cal.get(Calendar.YEAR);
    }

    /**
     * @return the month starting from 0 (i.e. 0-11)
     */
    public int getMonth() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
        return cal.get(Calendar.MONTH);
    }

    /**
     * @return the day of the month (1-31)
     */
    public int getDay() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(mTimestamp);
            return cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean equals(Object obj) {
        return ((MyDate) obj).getTimestamp() == mTimestamp;
    }
}
