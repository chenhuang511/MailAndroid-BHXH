package vn.bhxh.bhxhmail;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static final String DATE_DD_MM = ", dd/MM";

    public static boolean isStringBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String convertDateToString(Context context, Date source, String format) {
        if (source == null) return null;
        String[] dayOfMonth = context.getResources().getStringArray(R.array.c_day_of_month);
        DateFormat df = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(source);
        return dayOfMonth[calendar.get(Calendar.DAY_OF_WEEK) - 1] + df.format(source);
    }

}
