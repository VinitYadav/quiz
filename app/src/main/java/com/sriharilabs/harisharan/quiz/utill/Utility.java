package com.sriharilabs.harisharan.quiz.utill;

import android.app.Activity;
import android.widget.Toast;

public class Utility {
    public static void showToast(Activity activity,String message){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show();
    }
}