package org.snailclient.activity.utils.fps;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by wubo1 on 2016/7/11.
 */

public abstract class ToastUtil {

    private static void _showToast(final Context context, final int textid,
                                   final String text, final int delay, final boolean allowToastQueue) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                synchronized (ToastUtil.class) {
                    Toast toast = null;
                    if (textid == -1) {
                        toast = Toast.makeText(context, text, delay);
                    } else {
                        toast = Toast.makeText(context, textid, delay);
                    }
                    toast.show();
                }
            }
        });
    }

    private static void _showToast(final Context context, final int textid,
                                   final String text, final int delay, final String gravity,
                                   final boolean allowToastQueue) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                synchronized (ToastUtil.class) {
                    Toast toast = null;
                    if (textid == -1) {
                        toast = Toast.makeText(context, text, delay);
                    } else {
                        toast = Toast.makeText(context, textid, delay);
                    }
                    if (gravity.equals("center")) {
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }else{
                        toast.show();
                    }
                }
            }
        });
    }

    private static void _showToast(final Context context, final int textid,
                                   final String text, final int delay, final boolean allowToastQueue,
                                   final int gravity, final int xOffset, final int yOffset) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                synchronized (ToastUtil.class) {
                    Toast toast = null;
                    if (textid == -1) {
                        toast = Toast.makeText(context, text, delay);
                    } else {
                        toast = Toast.makeText(context, textid, delay);
                    }
                    toast.setGravity(gravity, xOffset, yOffset);
                    toast.show();
                }
            }
        });
    }

    public static void ShowShortToast(final Context context, final int textid) {
        _showToast(context, textid, null, Toast.LENGTH_SHORT, false);
    }

    public static void ShowShortToast(final Context context, final String text) {
        _showToast(context, -1, text, Toast.LENGTH_SHORT, false);
    }

    public static void ShowLongToast(final Context context, final int textid) {
        _showToast(context, textid, null, Toast.LENGTH_LONG, false);

    }

    public static void ShowLongToast(final Context context, final String text) {
        _showToast(context, -1, text, Toast.LENGTH_LONG, false);
    }

    public static void ShowLongToast(final Context context, final String text,
                                     final int gravity, final int xOffset, final int yOffset) {
        _showToast(context, -1, text, Toast.LENGTH_LONG, false, gravity,
                xOffset, yOffset);
    }

    public static void ShowLongToast(final Context context, final String text,
                                     String gravity) {
        if (gravity.equals("center")) {
            _showToast(context, -1, text, Toast.LENGTH_LONG, "center", false);
        } else {
            _showToast(context, -1, text, Toast.LENGTH_LONG, "bottom", false);
        }
    }

    /**
     *
     * @param context
     * @param textid
     * @param allowToastQueue
     *            是否允许Toast等待显示, 如果不允许, 3秒内的第二条Toast将不被显示
     */
    public static void ShowShortToast(final Context context, final int textid,
                                      boolean allowToastQueue) {
        _showToast(context, textid, null, Toast.LENGTH_SHORT, allowToastQueue);
    }

    /**
     *
     * @param context
     * @param text
     * @param allowToastQueue
     *            是否允许Toast等待显示, 如果不允许, 3秒内的第二条Toast将不被显示
     */
    public static void ShowShortToast(final Context context, final String text,
                                      boolean allowToastQueue) {
        _showToast(context, -1, text, Toast.LENGTH_SHORT, allowToastQueue);
    }

    /**
     *
     * @param context
     * @param textid
     * @param allowToastQueue
     *            是否允许Toast等待显示, 如果不允许, 3秒内的第二条Toast将不被显示
     */
    public static void ShowLongToast(final Context context, final int textid,
                                     boolean allowToastQueue) {
        _showToast(context, textid, null, Toast.LENGTH_LONG, allowToastQueue);

    }

    /**
     *
     * @param context
     * @param text
     * @param allowToastQueue
     *            是否允许Toast等待显示, 如果不允许, 3秒内的第二条Toast将不被显示
     */
    public static void ShowLongToast(final Context context, final String text,
                                     boolean allowToastQueue) {
        _showToast(context, -1, text, Toast.LENGTH_LONG, allowToastQueue);
    }

}
