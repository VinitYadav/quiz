package com.sriharilabs.harisharan.quiz.activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityCongratulationBinding;

public class CongratulationActivity extends AppCompatActivity {

    private ActivityCongratulationBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_congratulation);
        mBinding.setActivity(this);
        getDataFromBundle();
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
}
