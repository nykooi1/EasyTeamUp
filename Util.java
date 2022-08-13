package com.example.easyteamup;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.util.Locale;

public class Util {
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static String formatTimestamp(Timestamp timestamp) {
        String s = timestamp.toString();
        s = s.substring(0, s.indexOf("."));
        String[] dateTime = s.split(" ");
        String date = dateTime[0];
        String time = dateTime[1];
        String[] YMD = date.split("-");
        String[] HMS = time.split(":");
        int month = Integer.parseInt(YMD[1]);
        String monthString = new DateFormatSymbols().getMonths()[month-1];
        if(month == 9) {
            monthString = monthString.substring(0, 4) + ".";
        }
        else if(month != 5) {
            monthString = monthString.substring(0, 3) + ".";
        }
        int hour = Integer.parseInt(HMS[0]);
        String timeSuffix = (hour >= 0 && hour < 12) ? "am" : "pm";
        if(hour == 0) {
            hour += 12;
        }
        else if(hour > 12) {
            hour -= 12;
        }
        return String.format(Locale.ENGLISH, "%s %s, %s %d:%s%s", monthString, YMD[2], YMD[0], hour, HMS[1], timeSuffix);
    }

    public static SpannableString toUnderlinedString(String s) {
        SpannableString u = new SpannableString(s);
        u.setSpan(new UnderlineSpan(), 0, u.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return u;
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    public static String getEditTextString(EditText et) {
        return et.getText().toString();
    }

    public static boolean editTextIsEmpty(EditText et) {
        String content = et.getText().toString();
        return content.trim().equals("");
    }

    public static String getUUID() {
        return String.valueOf(java.util.UUID.randomUUID());
    }
}
