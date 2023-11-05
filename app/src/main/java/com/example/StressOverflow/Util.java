package com.example.StressOverflow;

import android.content.Context;
import android.widget.Toast;

public final class Util {
    /**
     * Upper bound for maximum character length for a name for a single item
     */
    public static final int MAX_ITEM_NAME_LENGTH = 60;

    /**
     * How many characters do we expect to fit on a single line for the description
     * field on the main list activity? (Of course tentative amount for now)
     */
    public static final int MAX_LINE_LENGTH = 30;

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
