package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.database.DatabaseHandler;
import com.sriharilabs.harisharan.quiz.database.QuestionBean;
import com.sriharilabs.harisharan.quiz.databinding.ActivityHomeBinding;
import com.sriharilabs.harisharan.quiz.utill.Constant;
import com.sriharilabs.harisharan.quiz.utill.PreferenceConnector;
import com.sriharilabs.harisharan.quiz.utill.Utility;

import java.util.List;

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
        String isDb = PreferenceConnector.readString(HomeActivity.this, PreferenceConnector.IS_DB, "");
        if (TextUtils.isEmpty(isDb)) {
            initDatabase();
        }
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
        DatabaseHandler db = new DatabaseHandler(this);
        db.addContact(new QuestionBean(1, "Abbott Labs",
                "abbott_labs_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(2, "Adidas",
                "adidas_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(3, "Adobe",
                "adobe_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(4, "Airbnb",
                "airbnb_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(5, "American Airlines",
                "american_airlines_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(6, "Applied Materials",
                "applied_materials_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(7, "Banco do Brasil",
                "banco_do_brasil_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(8, "Credit Agricole",
                "credit_agricole_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(9, "Daikin",
                "daikin_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(10, "Dove",
                "dove_icon", Constant.TYPE_LOGO));
        db.addContact(new QuestionBean(11, "Amitabh",
                "amit_ji_icon", Constant.TYPE_MALE));
        db.addContact(new QuestionBean(12, "Shardha",
                "shardha_icon", Constant.TYPE_FEMALE));
        db.addContact(new QuestionBean(13, "Akshay Kumar",
                "akki_icon", Constant.TYPE_MALE));
        db.addContact(new QuestionBean(14, "Disha Patani",
                "disha_icon", Constant.TYPE_FEMALE));
        /*List<QuestionBean> questionList = db.getAllContacts();
        if (questionList.size()>0){
            Utility.showToast(HomeActivity.this,"Question added");
        }*/
        PreferenceConnector.writeString(HomeActivity.this, PreferenceConnector.IS_DB, "true");
    }
}
