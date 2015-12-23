package org.dsa.iot.historian.utils;

import org.dsa.iot.dslink.util.TimeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author Samuel Grenier
 */
public class TimeParser {

    private static final ThreadLocal<DateFormat> FORMAT_TIME_ZONE;

    public static long parse(String time) {
        try {
            time = TimeUtils.fixTime(time);
            return FORMAT_TIME_ZONE.get().parse(time).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parse(long time) {
        return TimeUtils.format(time);
    }

    static {
        FORMAT_TIME_ZONE = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                String pattern = TimeUtils.getTimePatternTz();
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf;
            }
        };
    }
}
