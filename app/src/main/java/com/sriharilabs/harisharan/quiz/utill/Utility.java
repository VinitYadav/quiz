package com.sriharilabs.harisharan.quiz.utill;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

public class Utility {

    /**
     * Show toast
     */
    public static void showToast(Activity activity,String message){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show();
    }

    /**
     * Vibrate device
     */
    public static void vibrate(Activity activity){
        Vibrator vibe = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);
    }
}