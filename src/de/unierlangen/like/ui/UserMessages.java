package de.unierlangen.like.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import com.github.androidutils.logger.Logger;

public class UserMessages {
    private static final Logger log = Logger.getDefaultLogger();

    /**
     * Displays simple Alert Dialog and send message with what=-1 to handler
     * 
     * @param resourceId
     * @param context
     * @param handler
     */
    public static void displayErrorAndSendErrorMessage(int resourceId, Context context,
            final Handler handler) {
        log.d("displayError() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error");
        builder.setMessage(resourceId);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                handler.sendEmptyMessage(-1);
            }
        });
        builder.show();
    }

    /**
     * Display simple alert dialog
     * 
     * @param resourceId
     * @param context
     */
    public static void displayAlertDialog(int resourceId, Context context) {
        log.d("displayError() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Alert");
        builder.setMessage(resourceId);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Shows toast message
     * 
     * @param String
     *            message
     */
    public static void showMsg(String message, Context context) {
        Toast msg = Toast.makeText(context, message, Toast.LENGTH_LONG);
        msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
        msg.show();
    }

}
