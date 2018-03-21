package com.sriharilabs.harisharan.quiz.activity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.database.QuestionBean;
import com.sriharilabs.harisharan.quiz.database.SqlLiteDbHelper;
import com.sriharilabs.harisharan.quiz.databinding.ActivityHomeBinding;
import com.sriharilabs.harisharan.quiz.utill.PreferenceConnector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.sriharilabs.harisharan.quiz.utill.PreferenceConnector.readString;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        mBinding.setActivity(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setScore();
        PreferenceConnector.writeString(HomeActivity.this,
                PreferenceConnector.IS_CONGRATULATION, "");
    }

    /**
     * Click on start button
     */
    public void onClickStart() {
        // Open play screen
        Intent intent = new Intent(HomeActivity.this, PlayActivity.class);
        startActivity(intent);
    }

    private void init() {
        String isDb = PreferenceConnector.readString(HomeActivity.this,
                PreferenceConnector.IS_DB, "");
        if (TextUtils.isEmpty(isDb)) {
            initDatabase();
        }

        mBinding.imageViewStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mBinding.imageViewStart.setImageResource(R.drawable.start_on_icon);
                    Intent intent = new Intent(HomeActivity.this, PlayActivity.class);
                    startActivity(intent);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBinding.imageViewStart.setImageResource(R.drawable.start_off_icon);
                }
                return true;
            }
        });
    }

    /**
     * Set score
     */
    private void setScore() {
        String score = readString(HomeActivity.this, PreferenceConnector.HIGH_SCORE, "");
        if (TextUtils.isEmpty(score) || score.equalsIgnoreCase("0")) {
            mBinding.textViewScore.setText("0");
        } else {
            mBinding.textViewScore.setText(score);
        }
    }

    /**
     * Initialize database
     */
    private void initDatabase() {
        SqlLiteDbHelper dbHelper;
        dbHelper = new SqlLiteDbHelper(this);
        dbHelper.openDataBase();
        ArrayList<QuestionBean> questionList = dbHelper.getAllQuestion();
        if (questionList.size() > 0) {
            PreferenceConnector.writeString(HomeActivity.this, PreferenceConnector.IS_DB, "true");
        }
    }
}
