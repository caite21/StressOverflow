package com.example.StressOverflow;

import android.content.Context;
import android.widget.Toast;

import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Tag.Tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public final class Util {

    /**
     * Returns a mock item with test fields labelled by val
     *
     * @param val integer label to use
     * @return temporary Item object with val everywhere
     */
    public static Item dummyItem(int val) {
        return new Item(
                String.format("TestObject%s", String.valueOf(val)),
                String.format("TestMake%s", String.valueOf(val)),
                String.format("TestModel%s", String.valueOf(val)),
                String.format("TestDescription%s", String.valueOf(val)),
                new GregorianCalendar(2022, 1, 2),
                (double) val,
                String.format("TestComment%s", String.valueOf(val)),
                new ArrayList<Tag>(),
                new ArrayList<String>(),
                String.valueOf(val),
                "Test Owner"
        );
    }

    public static boolean isDateValid(Integer year, Integer month, Integer day) {
        try {
            if (year < 0 || day < 0 || month < 0 || month > 12) {
                return false;
            }
            if (month == 2 && year % 4 != 0 && day > 28) {
                return false;
            }
            if (month == 2 && year % 4 == 0 && day > 29) {
                return false;
            }
            if ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) {
                return false;
            }
            return day <= 30;
        } catch (Exception e) {
            return false;
        }
    }
    public static final int MAX_ITEM_NAME_LENGTH = 60;

    /**
     * How many characters do we expect to fit on a single line for the description
     * field on the main list activity? (Of course tentative amount for now)
     */
    public static final int MAX_LINE_LENGTH = 300;

    /**
     * Displays a short toast at the bottom of the screen.
     *
     * Useful for brief user feedback which contains little text.
     *
     * @param context context
     * @param text text
     */
    public static void showShortToast(Context context, String text) {
        Toast msg = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        msg.show();
    }

    /**
     * Displays a long toast at the bottom of the screen.
     *
     * Useful for brief user feedback which contains significant text.
     *
     * @param context context
     * @param text text
     */
    public static void showLongToast(Context context, String text) {
        Toast msg = Toast.makeText(context, text, Toast.LENGTH_LONG);
        msg.show();
    }
}
