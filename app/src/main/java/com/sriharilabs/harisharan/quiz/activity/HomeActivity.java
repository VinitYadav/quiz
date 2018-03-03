package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityHomeBinding;
import com.sriharilabs.harisharan.quiz.utill.PreferenceConnector;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        mBinding.setActivity(this);
        init();
    }

    /**
     * Click on start button
     */
    public void onClickStart() {
        // Open play screen
        Intent intent = new Intent(HomeActivity.this,PlayActivity.class);
        startActivity(intent);
    }

    private void init() {
        String score = PreferenceConnector.readString(HomeActivity.this, PreferenceConnector.HIGH_SCORE, "");
        if (TextUtils.isEmpty(score) || score.equalsIgnoreCase("0")) {
            mBinding.textViewScore.setText("0");
        } else {
            mBinding.textViewScore.setText(score);
        }
    }
}
