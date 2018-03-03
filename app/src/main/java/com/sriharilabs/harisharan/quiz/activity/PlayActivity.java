package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityPlayBinding;

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding mBinding;
    private CountDownTimer countDownTimer;
    private int POINT = 30;
    private int LIFE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);
        mBinding.setActivity(this);
        init();
    }

    /**
     * Click on answer
     */
    public void onClickAnswer(int which) {
        switch (which) {
            case 1: // A
                Intent intent = new Intent(PlayActivity.this, GameOverActivity.class);
                startActivity(intent);
                break;
            case 2: // B
                countDownTimer.start();
                break;
            case 3: // C
                break;
            case 4: // D
                break;
        }
    }

    private void init() {
        setLife(); // Set life
        setPoint(); // Set point
        initTimer(); // Set time
    }

    /**
     * Initialize time
     */
    private void initTimer() {
        countDownTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                mBinding.textViewTime.setText("Time: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                mBinding.textViewTime.setText("Time: " + "0");
            }

        }.start();
    }

    /**
     * Set point
     */
    private void setPoint() {
        mBinding.textViewPoints.setText("" + POINT);
    }

    /**
     * Set life
     */
    private void setLife() {
        mBinding.textViewLife.setText("" + LIFE);
    }
}
