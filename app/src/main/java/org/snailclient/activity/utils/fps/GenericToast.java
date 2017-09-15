package org.snailclient.activity.utils.fps;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class GenericToast{
    private static final String TAG = "GenericToast";

    /** {@link Toast#LENGTH_SHORT} default time is 3500ms */
    private static final int LENGTH_SHORT_TIME = 2000;

    private static Context mContext = null;

    private static Toast mToast = null;
    private static int mDuration = 0;
    private static CharSequence mText = null;

    private Handler mHandler = new Handler();

    private GenericToast(Context context) {
        mContext = context;
    }

    public static GenericToast makeText(Context context, CharSequence text, int duration){
        GenericToast instance = new GenericToast(context);
        mContext = context;
        mDuration = duration;
        mText = text;
        return instance;
    }

    public void show(){
        Log.d(TAG, "Show custom toast");
        mHandler.post(showRunnable);
    }

    public void hide(){
        Log.d(TAG, "Hide custom toast");
        mDuration = 0;
        if(mToast != null){
            mToast.cancel();
        }
    }

    private Runnable showRunnable = new Runnable(){
        @Override
        public void run() {
            if(mToast != null){
                mToast.setText(mText);
            }else{
                mToast = Toast.makeText(mContext, null, Toast.LENGTH_LONG);
                mToast.setText(mText);
            }
            if(mDuration != 0){
                mToast.show();
            }else{
                Log.d(TAG, "Hide custom toast in runnable");
                hide();
                return;
            }

            if(mDuration > LENGTH_SHORT_TIME){
                mHandler.postDelayed(showRunnable, LENGTH_SHORT_TIME);
                mDuration -= LENGTH_SHORT_TIME;
            }else{
                mHandler.postDelayed(showRunnable, mDuration);
                mDuration = 0;
            }
        }
    };
}
