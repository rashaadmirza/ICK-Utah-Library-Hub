package edu.northeastern.ickutah.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class UiUtils {

    /**
     * Displays a Toast message at the bottom of the screen
     */
    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 195);
        toast.show();
    }

    /**
     * Convert dp to pixels (for setting margins dynamically)
     */
    public static int dpToPx(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}