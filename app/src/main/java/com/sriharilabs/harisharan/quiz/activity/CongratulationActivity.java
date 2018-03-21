package com.sriharilabs.harisharan.quiz.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityCongratulationBinding;

public class CongratulationActivity extends AppCompatActivity {

    private ActivityCongratulationBinding mBinding;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_congratulation);
        mBinding.setActivity(this);
        getDataFromBundle();
        initTimer();
    }

    /**
     * Get data from bundle
     */
    private void getDataFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBinding.textViewScore.setText(bundle.getString("point"));
        }
    }

    /**
     * Initialize time
     */
    private void initTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                mBinding.textViewTime.setText("Time: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                countDownTimer.cancel();
                mBinding.textViewTime.setText("Time: " + "0");
                finish();
            }
        }.start();
    }
}
