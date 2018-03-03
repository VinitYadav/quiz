package com.sriharilabs.harisharan.quiz.activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityGameOverBinding;

public class GameOverActivity extends AppCompatActivity {

    private ActivityGameOverBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_game_over);
        mBinding.setActivity(this);
    }
}
