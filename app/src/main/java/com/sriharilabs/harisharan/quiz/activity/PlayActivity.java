package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.database.QuestionBean;
import com.sriharilabs.harisharan.quiz.database.SqlLiteDbHelper;
import com.sriharilabs.harisharan.quiz.databinding.ActivityPlayBinding;
import com.sriharilabs.harisharan.quiz.utill.Constant;
import com.sriharilabs.harisharan.quiz.utill.PreferenceConnector;
import com.sriharilabs.harisharan.quiz.utill.Utility;

import java.util.ArrayList;
import java.util.Collections;

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding mBinding;
    private CountDownTimer countDownTimer;
    private int POINT = 0;
    private int LIFE = 3;
    private ArrayList<QuestionBean> questionList = new ArrayList<>();
    private int questionNumber = 0;
    private int answerPosition = 0;
    private String RIGHT_ANSWER = "";
    private final int TOTAL_QUESTION = 236;
    private boolean wrongAnswerFlag;
    private ArrayList<QuestionBean> tempList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);
        mBinding.setActivity(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        countDownTimer = null;
    }

    /**
     * Click on answer
     */
    public void onClickAnswer(int which) {
        if (wrongAnswerFlag) {
            return;
        }
        switch (which) {
            case 1: // A
                rightAnswer(mBinding.textViewAnswerA.getText().toString().trim(), 1);
                break;
            case 2: // B
                rightAnswer(mBinding.textViewAnswerB.getText().toString().trim(), 2);
                break;
            case 3: // C
                rightAnswer(mBinding.textViewAnswerC.getText().toString().trim(), 3);
                break;
            case 4: // D
                rightAnswer(mBinding.textViewAnswerD.getText().toString().trim(), 4);
                break;
        }
    }

    private void init() {
        setLife(); // Set life
        setPoint(); // Set point
        getAllQuestion(); // Get all question list
        initTimer(); // Set time
        makeQuestion(); // Make new question
        bottomAd(); // Initialize bottom ad
    }

    /**
     * Initialize bottom ad
     */
    private void bottomAd() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this,getString(R.string.ad_unit));
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.ad_unit_id_bottom));
        AdRequest adRequest = new AdRequest.Builder().build();
        mBinding.adView.loadAd(adRequest);
    }

    /**
     * Initialize time
     */
    private void initTimer() {
        countDownTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                mBinding.textViewTime.setText("Time: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                countDownTimer.cancel();
                checkGameOver();
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

    /**
     * Check game over condition
     */
    private void checkGameOver() {
        LIFE--;
        if (LIFE < 0) {
            LIFE = 0;
        }
        setLife();
        if (LIFE <= 0) {
            countDownTimer.cancel();
            openGameOverScreen();
        } else {
            if (!wrongAnswerFlag) {
                makeQuestion();
            }
        }
    }

    /**
     * Open game over screen
     */
    private void openGameOverScreen() {
        if (!isDestroyed()) {
            saveHighScore();
            Intent intent = new Intent(PlayActivity.this, GameOverActivity.class);
            intent.putExtra("point", POINT + "");
            startActivity(intent);
            finish();
        }
    }

    /**
     * Get all question
     */
    private void getAllQuestion() {
        SqlLiteDbHelper dbHelper;
        dbHelper = new SqlLiteDbHelper(this);
        dbHelper.openDataBase();
        questionList = dbHelper.getAllQuestion();
        if (questionList.size() > 0) {
            Collections.shuffle(questionList);
            tempList = new ArrayList<>(questionList);
        }
    }

    /**
     * Make question
     */
    private void makeQuestion() {
        if (questionList == null || questionList.size() == 0 || LIFE <= 0) {
            return;
        }
        openCongratulationScreen();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed()) {
                    return;
                }
                wrongAnswerFlag = false;
                countDownTimer.cancel();
                countDownTimer.start();
                resetAnswer();
                String type = questionList.get(questionNumber).getType();
                if (!TextUtils.isEmpty(type)) { // Check type
                    switch (type) {
                        case Constant.TYPE_LOGO:
                            setQuestion(getString(R.string.logo_question));
                            break;
                        case Constant.TYPE_MALE:
                            setQuestion(getString(R.string.logo_actor));
                            break;
                        case Constant.TYPE_FEMALE:
                            setQuestion(getString(R.string.logo_actress));
                            break;
                    }
                }
                setAnswer();
                setImage();
                if (questionNumber <= (questionList.size() - 1)) {
                    questionNumber++;
                }
            }
        }, 500);
    }

    /**
     * Set question
     */
    private void setQuestion(String question) {
        mBinding.textViewQuestion.setText(question);
    }

    /**
     * Set answer
     */
    private void setAnswer() {
        String answer = questionList.get(questionNumber).getAnswer();
        RIGHT_ANSWER = answer;
        answerPosition = getAnswerPosition();
        switch (answerPosition) { // Set correct answer
            case 1: // A
                mBinding.textViewAnswerA.setText(answer);
                setOtherAnswer(1);
                break;
            case 2: // B
                mBinding.textViewAnswerB.setText(answer);
                setOtherAnswer(2);
                break;
            case 3: // C
                mBinding.textViewAnswerC.setText(answer);
                setOtherAnswer(3);
                break;
            case 4: // D
                mBinding.textViewAnswerD.setText(answer);
                setOtherAnswer(4);
                break;
        }

    }

    /**
     * Get random number for answer position
     */
    private int getAnswerPosition() {
        return (int) ((Math.random() * 4) + 1);
    }

    /**
     * Get other answer
     */
    private ArrayList<String> getOtherAnswer() {
        Collections.shuffle(tempList);
        String answer = tempList.get(questionNumber).getAnswer();
        String selectType = tempList.get(questionNumber).getType();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < tempList.size(); i++) {
            String type = tempList.get(i).getType();
            if (type.equalsIgnoreCase(selectType) &&
                    !answer.equalsIgnoreCase(tempList.get(i).getAnswer())) {
                list.add(tempList.get(i).getAnswer());
                if (list.size() >= 3) {
                    break;
                }
            }
        }
        return list;
    }

    /**
     * Set other answer
     */
    private void setOtherAnswer(int position) {
        if (getOtherAnswer() != null && getOtherAnswer().size() > 0) {
            switch (position) {
                case 1: // A
                    mBinding.textViewAnswerB.setText(getOtherAnswer().get(0));
                    mBinding.textViewAnswerC.setText(getOtherAnswer().get(1));
                    mBinding.textViewAnswerD.setText(getOtherAnswer().get(2));
                    break;
                case 2: // B
                    mBinding.textViewAnswerA.setText(getOtherAnswer().get(0));
                    mBinding.textViewAnswerC.setText(getOtherAnswer().get(1));
                    mBinding.textViewAnswerD.setText(getOtherAnswer().get(2));
                    break;
                case 3: // C
                    mBinding.textViewAnswerA.setText(getOtherAnswer().get(0));
                    mBinding.textViewAnswerB.setText(getOtherAnswer().get(1));
                    mBinding.textViewAnswerD.setText(getOtherAnswer().get(2));
                    break;
                case 4: // D
                    mBinding.textViewAnswerA.setText(getOtherAnswer().get(0));
                    mBinding.textViewAnswerB.setText(getOtherAnswer().get(1));
                    mBinding.textViewAnswerC.setText(getOtherAnswer().get(2));
                    break;
            }
        }
    }


    /**
     * Check right answer
     */
    private void rightAnswer(String answer, int which) {
        if (!TextUtils.isEmpty(answer)) {
            if (answer.equalsIgnoreCase(RIGHT_ANSWER)) {
                setRightAnswerBackground(which);
                POINT++;
                setPoint();
                makeQuestion();
            } else {
                setWrongAnswerBackground();
            }
        }
    }

    /**
     * Set wrong answer background
     */
    private void setWrongAnswerBackground() {
        switch (answerPosition) {
            case 1: // A
                mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect_orange);
                break;
            case 2: // B
                mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect_orange);
                break;
            case 3: // C
                mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect_orange);
                break;
            case 4: // D
                mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect_orange);
                mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect_green);
                break;
        }
        mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
        mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
        mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
        mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
        Utility.vibrate(PlayActivity.this);
        wrongAnswerFlag = true;
        countDownTimer.cancel();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGameOver();
                makeQuestion();
            }
        }, 1000);
    }

    /**
     * Set wrong answer background
     */
    private void setRightAnswerBackground(int which) {
        switch (which) {
            case 1: // A
                mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                break;
            case 2: // B
                mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                break;
            case 3: // C
                mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                break;
            case 4: // D
                mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect_green);
                mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
                mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.white));
                break;
        }
    }

    /**
     * Reset answer background
     */
    private void resetAnswer() {
        mBinding.relativeLayoutA.setBackgroundResource(R.drawable.answer_round_rect);
        mBinding.relativeLayoutB.setBackgroundResource(R.drawable.answer_round_rect);
        mBinding.relativeLayoutC.setBackgroundResource(R.drawable.answer_round_rect);
        mBinding.relativeLayoutD.setBackgroundResource(R.drawable.answer_round_rect);
        mBinding.textViewAnswerA.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
        mBinding.textViewAnswerB.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
        mBinding.textViewAnswerC.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
        mBinding.textViewAnswerD.setTextColor(ContextCompat.getColor(PlayActivity.this, R.color.red));
    }

    /**
     * Open congratulation screen
     */
    private void openCongratulationScreen() {
        if (!isDestroyed()) {
            //int size = questionList.size();
            if (questionNumber >= TOTAL_QUESTION) {
                saveHighScore();
                Intent intent = new Intent(PlayActivity.this, CongratulationActivity.class);
                intent.putExtra("point", POINT + "");
                startActivity(intent);
                finish();
            }
        }
    }

    /**
     * Set high score in local db
     */
    private void saveHighScore() {
        String pointTemp = PreferenceConnector.readString(PlayActivity.this, PreferenceConnector.HIGH_SCORE, "");
        if (TextUtils.isEmpty(pointTemp)) {
            PreferenceConnector.writeString(PlayActivity.this, PreferenceConnector.HIGH_SCORE, POINT + "");
        } else {
            int temp = Integer.parseInt(pointTemp);
            if (POINT > temp) {
                PreferenceConnector.writeString(PlayActivity.this, PreferenceConnector.HIGH_SCORE, POINT + "");
            }
        }
    }

    /**
     * Set image
     */
    private void setImage() {
        String image = questionList.get(questionNumber).getImage();
        if (!TextUtils.isEmpty(image)) {
            switch (image) {
                case "aaidu_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.aaidu_icon);
                    break;
                case "abbott_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.abbott_icon);
                    break;
                case "adidas_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.adidas_icon);
                    break;
                case "abn_amro_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.abn_amro_icon);
                    break;
                case "accenture_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.accenture_icon);
                    break;
                case "airbnb_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.airbnb_icon);
                    break;
                case "american_airlines_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.american_airlines_icon);
                    break;
                case "adobe_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.applied_materials_icon);
                    break;
                case "adp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.adp_icon);
                    break;
                case "agricultural_bank_of_china_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.agricultural_bank_of_china_icon);
                    break;
                case "aia_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.aia_icon);
                    break;
                case "dove_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dove_icon);
                    break;
                case "airtel_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.airtel_icon);
                    break;
                case "aldi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.aldi_icon);
                    break;
                case "alibaba_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.alibaba_icon);
                    break;
                case "allianz_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.allianz_icon);
                    break;
                case "allstate_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.allstate_icon);
                    break;
                case "anz_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.anz_icon);
                    break;
                case "applied_materials_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.applied_materials_icon);
                    break;
                case "at_and_t_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.at_and_t_icon);
                    break;
                case "au_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.au_icon);
                    break;
                case "audi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.audi_icon);
                    break;
                case "autozone_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.autozone_icon);
                    break;
                case "aviva_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.aviva_icon);
                    break;
                case "banco_do_brasil_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.banco_do_brasil_icon);
                    break;
                case "bank_of_america_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_america_icon);
                    break;
                case "bank_of_america_merrill_lynch_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_america_merrill_lynch_icon);
                    break;
                case "bank_of_china_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_china_icon);
                    break;
                case "bank_of_communications_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_communications_icon);
                    break;
                case "bank_of_montreal_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_montreal_icon);
                    break;
                case "barclays_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.barclays_icon);
                    break;
                case "basf_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.basf_icon);
                    break;
                case "bayer_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bayer_icon);
                    break;
                case "blogger_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.blogger_icon);
                    break;
                case "bnp_paribas_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bnp_paribas_icon);
                    break;
                case "boeing_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.boeing_icon);
                    break;
                case "bosch_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bosch_icon);
                    break;
                case "bp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bp_icon);
                    break;
                case "bradesco_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bradesco_icon);
                    break;
                case "bridgestone_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bridgestone_icon);
                    break;
                case "broadcom_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.broadcom_icon);
                    break;
                case "bt_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bt_icon);
                    break;
                case "carrefour_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.carrefour_icon);
                    break;
                case "caterpillar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.caterpillar_icon);
                    break;
                case "cbs_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cbs_icon);
                    break;
                case "cccc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cccc_icon);
                    break;
                case "centurylink_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.centurylink_icon);
                    break;
                case "cerner_corp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cerner_corp_icon);
                    break;
                case "chase_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.chase_icon);
                    break;
                case "chevrolet_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.chevrolet_icon);
                    break;
                case "chevron_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.chevron_icon);
                    break;
                case "china_cinda_h_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.china_cinda_h_icon);
                    break;
                case "china_construction_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.china_construction_bank_icon);
                    break;
                case "china_merchants_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.china_merchants_bank_icon);
                    break;
                case "china_mobile_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.china_mobile_icon);
                    break;
                case "china_telecom_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.china_telecom_icon);
                    break;
                case "cisco_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cisco_icon);
                    break;
                case "citi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.citi_icon);
                    break;
                case "cj_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cj_group_icon);
                    break;
                case "cognizant_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cognizant_icon);
                    break;
                case "commonwealth_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.commonwealth_bank_icon);
                    break;
                case "conocochillips_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.conocochillips_icon);
                    break;
                case "credit_agricole_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.credit_agricole_icon);
                    break;
                case "cummins_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cummins_icon);
                    break;
                case "cvs_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cvs_icon);
                    break;
                case "daikin_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.daikin_icon);
                    break;
                case "daiwa_house_industry_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.daiwa_house_industry_icon);
                    break;
                case "danone_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.danone_icon);
                    break;
                case "dbs_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dbs_icon);
                    break;
                case "dell_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dell_icon);
                    break;
                case "delta_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.delta_icon);
                    break;
                case "deutsche_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.deutsche_bank_icon);
                    break;
                case "discover_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.discover_icon);
                    break;
                case "dominos_pizza_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dominos_pizza_icon);
                    break;
                case "dropbox_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dropbox_icon);
                    break;
                case "dxc_technology_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dxc_technology_icon);
                    break;
                case "e_leclerc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.e_leclerc_icon);
                    break;
                case "edf_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.edf_icon);
                    break;
                case "electronic_arts_ea_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.electronic_arts_ea_icon);
                    break;
                case "emerson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.emerson_icon);
                    break;
                case "emirates_airlines_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.emirates_airlines_icon);
                    break;
                case "ericsson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ericsson_icon);
                    break;
                case "ey_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ey_icon);
                    break;
                case "facebook_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.facebook_icon);
                    break;
                case "ferrari_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ferrari_icon);
                    break;
                case "ford_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ford_icon);
                    break;
                case "fresenius_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.fresenius_icon);
                    break;
                case "fubon_life_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.fubon_life_icon);
                    break;
                case "garnier_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.garnier_icon);
                    break;
                case "gatorade_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gatorade_icon);
                    break;
                case "gazprom_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gazprom_icon);
                    break;
                case "ge_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ge_icon);
                    break;
                case "generali_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.generali_icon);
                    break;
                case "gree_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gree_icon);
                    break;
                case "gs_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gs_group_icon);
                    break;
                case "heineken_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.heineken_icon);
                    break;
                case "heinz_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.heinz_icon);
                    break;
                case "hp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.hp_icon);
                    break;
                case "hsbc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.hsbc_icon);
                    break;
                case "huawei_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.huawei_icon);
                    break;
                case "huggies_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.huggies_icon);
                    break;
                case "hyundai_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.hyundai_icon);
                    break;
                case "iberdrola_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.iberdrola_icon);
                    break;
                case "icbc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.icbc_icon);
                    break;
                case "ikea_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ikea_icon);
                    break;
                case "indian_oil_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.indian_oil_icon);
                    break;
                case "industrial_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.industrial_bank_icon);
                    break;
                case "ing_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ing_icon);
                    break;
                case "innogy_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.innogy_icon);
                    break;
                case "instagram_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.instagram_icon);
                    break;
                case "intel_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.intel_icon);
                    break;
                case "itau_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.itau_icon);
                    break;
                case "john_deere_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.john_deere_icon);
                    break;
                case "johnnie_walker_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.johnnie_walker_icon);
                    break;
                case "kb_financial_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kb_financial_group_icon);
                    break;
                case "kbc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kbc_icon);
                    break;
                case "kfc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kfc_icon);
                    break;
                case "korea_electric_power_corporation_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.korea_electric_power_corporation_icon);
                    break;
                case "larsen_and_toubro_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.larsen_and_toubro_icon);
                    break;
                case "lg_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.lg_group_icon);
                    break;
                case "lic_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.lic_icon);
                    break;
                case "linkedin_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.linkedin_icon);
                    break;
                case "logo_psd_recovered_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.logo_psd_recovered_icon);
                    break;
                case "louis_vuitton_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.louis_vuitton_icon);
                    break;
                case "marriott_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.marriott_icon);
                    break;
                case "mastercard_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mastercard_icon);
                    break;
                case "mccain_foods_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mccain_foods_icon);
                    break;
                case "mcdonalds_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mcdonalds_icon);
                    break;
                case "mclane_company_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mclane_company_icon);
                    break;
                case "mercedes_benz_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mercedes_benz_icon);
                    break;
                case "metlife_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.metlife_icon);
                    break;
                case "micromax_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.micromax_icon);
                    break;
                case "microsoft_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.microsoft_icon);
                    break;
                case "mitsubishi_conglomerate_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mitsubishi_conglomerate_icon);
                    break;
                case "mitsui_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mitsui_group_icon);
                    break;
                case "moutai_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.moutai_icon);
                    break;
                case "movistar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.movistar_icon);
                    break;
                case "natwest_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.natwest_icon);
                    break;
                case "three_mobile_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.three_mobile_icon);
                    break;
                case "apple_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.apple_icon);
                    break;
                case "nbc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nbc_icon);
                    break;
                case "nestle_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nestle_icon);
                    break;
                case "nike_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nike_icon);
                    break;
                case "nissan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nissan_icon);
                    break;
                case "nivea_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nivea_icon);
                    break;
                case "ntt_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ntt_group_icon);
                    break;
                case "nvidia_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.nvidia_icon);
                    break;
                case "o_two_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.o_two_icon);
                    break;
                case "ocbc_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ocbc_bank_icon);
                    break;
                case "pampers_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.pampers_icon);
                    break;
                case "paypal_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.paypal_icon);
                    break;
                case "pepsi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.pepsi_icon);
                    break;
                case "petronas_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.petronas_icon);
                    break;
                case "pfizer_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.pfizer_icon);
                    break;
                case "playstation_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.playstation_icon);
                    break;
                case "pnc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.pnc_icon);
                    break;
                case "polo_ralph_lauren_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.polo_ralph_lauren_icon);
                    break;
                case "porsche_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.porsche_icon);
                    break;
                case "prudential_uk_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.prudential_uk_icon);
                    break;
                case "prudential_us_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.prudential_us_icon);
                    break;
                case "ptt_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ptt_icon);
                    break;
                case "purina_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.purina_icon);
                    break;
                case "pwc_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.pwc_icon);
                    break;
                case "qnb_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.qnb_icon);
                    break;
                case "rabobank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rabobank_icon);
                    break;
                case "randstad_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.randstad_icon);
                    break;
                case "red_bull_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.red_bull_icon);
                    break;
                case "reliance_industries_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.reliance_industries_icon);
                    break;
                case "renault_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.renault_icon);
                    break;
                case "repsol_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.repsol_icon);
                    break;
                case "roche_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.roche_icon);
                    break;
                case "rogers_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rogers_icon);
                    break;
                case "rolex_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rolex_icon);
                    break;
                case "royal_caribbean_cruises_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.royal_caribbean_cruises_icon);
                    break;
                case "saint_gobain_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.saint_gobain_icon);
                    break;
                case "salesforce_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.salesforce_icon);
                    break;
                case "sams_club_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sams_club_icon);
                    break;
                case "sap_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sap_icon);
                    break;
                case "scotiabank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.scotiabank_icon);
                    break;
                case "shell_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.shell_icon);
                    break;
                case "sherwin_williams_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sherwin_williams_icon);
                    break;
                case "sk_group_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sk_group_icon);
                    break;
                case "snapchat_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.snapchat_icon);
                    break;
                case "societe_generale_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.societe_generale_icon);
                    break;
                case "sodexo_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sodexo_icon);
                    break;
                case "softbank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.softbank_icon);
                    break;
                case "sprint_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sprint_icon);
                    break;
                case "sprite_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sprite_icon);
                    break;
                case "standard_chartered_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.standard_chartered_icon);
                    break;
                case "starbucks_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.starbucks_icon);
                    break;
                case "state_bank_of_india_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.state_bank_of_india_icon);
                    break;
                case "state_grid_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.state_grid_icon);
                    break;
                case "statoil_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.statoil_icon);
                    break;
                case "subway_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.subway_icon);
                    break;
                case "suzuki_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.suzuki_icon);
                    break;
                case "swisscom_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.swisscom_icon);
                    break;
                case "t_deutsche_telekom_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.t_deutsche_telekom_icon);
                    break;
                case "target_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.target_icon);
                    break;
                case "tata_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tata_icon);
                    break;
                case "telenor_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.telenor_icon);
                    break;
                case "telstra_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.telstra_icon);
                    break;
                case "telus_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.telus_icon);
                    break;
                case "tesco_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tesco_icon);
                    break;
                case "tesla_motors_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tesla_motors_icon);
                    break;
                case "texas_instruments_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.texas_instruments_icon);
                    break;
                case "tim_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tim_icon);
                    break;
                case "total_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.total_icon);
                    break;
                case "toyota_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.toyota_icon);
                    break;
                case "travelers_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.travelers_icon);
                    break;
                case "tumblr_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tumblr_icon);
                    break;
                case "twitter_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.twitter_icon);
                    break;
                case "tyson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tyson_icon);
                    break;
                case "ubs_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ubs_icon);
                    break;
                case "under_armour_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.under_armour_icon);
                    break;
                case "union_pacific_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.union_pacific_icon);
                    break;
                case "universal_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.universal_icon);
                    break;
                case "universal_two_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.universal_two_icon);
                    break;
                case "uob_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.uob_icon);
                    break;
                case "ups_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ups_icon);
                    break;
                case "uq_communications_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.uq_communications_icon);
                    break;
                case "us_bank_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.us_bank_icon);
                    break;
                case "valero_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.valero_icon);
                    break;
                case "vimeo_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.vimeo_icon);
                    break;
                case "vinci_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.vinci_icon);
                    break;
                case "vodafone_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.vodafone_icon);
                    break;
                case "volkswagen_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.volkswagen_icon);
                    break;
                case "walmart_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.walmart_icon);
                    break;
                case "warner_bros_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.warner_bros_icon);
                    break;
                case "wechat_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.wechat_icon);
                    break;
                case "western_digital_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.western_digital_icon);
                    break;
                case "westpac_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.westpac_icon);
                    break;
                case "whatsapp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.whatsapp_icon);
                    break;
                case "winston_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.winston_icon);
                    break;
                case "xiaomi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.xiaomi_icon);
                    break;
                case "yahoo_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.yahoo_icon);
                    break;
                case "zalando_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.zalando_icon);
                    break;
                case "zurich_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.zurich_icon);
                    break;
                case "adele_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.adele_icon);
                    break;
                case "amy_schumer_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.amy_schumer_icon);
                    break;
                case "anushka_sharma_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.anushka_sharma_icon);
                    break;
                case "beyonce_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.beyonce_icon);
                    break;
                case "britney_spears_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.britney_spears_icon);
                    break;
                case "celine_dion_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.celine_dion_icon);
                    break;
                case "deepika_padukone_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.deepika_padukone_icon);
                    break;
                case "dolly_parton_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dolly_parton_icon);
                    break;
                case "ellen_degeneres_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ellen_degeneres_icon);
                    break;
                case "guns_n_roses_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.guns_n_roses_icon);
                    break;
                case "j_k_rowling_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.j_k_rowling_icon);
                    break;
                case "jennifer_lopez_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jennifer_lopez_icon);
                    break;
                case "judy_sheindlin_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.judy_sheindlin_icon);
                    break;
                case "katy_perry_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.katy_perry_icon);
                    break;
                case "kim_kardashian_west_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kim_kardashian_west_icon);
                    break;
                case "kylie_jenner_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kylie_jenner_icon);
                    break;
                case "p_v_sindhu_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.p_v_sindhu_icon);
                    break;
                case "priyanka_chopra_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.priyanka_chopra_icon);
                    break;
                case "rihanna_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rihanna_icon);
                    break;
                case "sofia_vergara_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sofia_vergara_icon);
                    break;
                case "taylor_swift_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.taylor_swift_icon);
                    break;
                case "a_r_rahman_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.a_r_rahman_icon);
                    break;
                case "aamir_khan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.aamir_khan_icon);
                    break;
                case "adam_sandle_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.adam_sandle_icon);
                    break;
                case "ajay_devgn_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ajay_devgn_icon);
                    break;
                case "akshay_kumar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.akshay_kumar_icon);
                    break;
                case "andrew_luck_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.andrew_luck_icon);
                    break;
                case "bill_oreilly_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bill_oreilly_icon);
                    break;
                case "billy_joel_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.billy_joel_icon);
                    break;
                case "bon_jovi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bon_jovi_icon);
                    break;
                case "bruce_springsteen_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bruce_springsteen_icon);
                    break;
                case "bruno_mars_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.bruno_mars_icon);
                    break;
                case "calvin_harris_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.calvin_harris_icon);
                    break;
                case "chance_the_rapper_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.chance_the_rapper_icon);
                    break;
                case "chris_rock_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.chris_rock_icon);
                    break;
                case "clayton_kershaw_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.clayton_kershaw_icon);
                    break;
                case "coldplay_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.coldplay_icon);
                    break;
                case "conor_mcregor_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.conor_mcregor_icon);
                    break;
                case "cristiano_ronaldo_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.cristiano_ronaldo_icon);
                    break;
                case "damian_lillard_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.damian_lillard_icon);
                    break;
                case "dave_chappelle_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dave_chappelle_icon);
                    break;
                case "david_copperfield_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.david_copperfield_icon);
                    break;
                case "derrick_rose_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.derrick_rose_icon);
                    break;
                case "dr_dre_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dr_dre_icon);
                    break;
                case "drake_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.drake_icon);
                    break;
                case "drew_brees_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.drew_brees_icon);
                    break;
                case "dwayne_johnson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dwayne_johnson_icon);
                    break;
                case "dwayne_wade_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dwayne_wade_icon);
                    break;
                case "ed_sheeran_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ed_sheeran_icon);
                    break;
                case "elton_john_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.elton_john_icon);
                    break;
                case "fernando_alonso_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.fernando_alonso_icon);
                    break;
                case "fletcher_cox_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.fletcher_cox_icon);
                    break;
                case "florida_georgia_line_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.florida_georgia_line_icon);
                    break;
                case "gareth_bale_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gareth_bale_icon);
                    break;
                case "garth_brooks_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.garth_brooks_icon);
                    break;
                case "gordon_ramsay_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.gordon_ramsay_icon);
                    break;
                case "howard_stern_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.howard_stern_icon);
                    break;
                case "hrithik_roshan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.hrithik_roshan_icon);
                    break;
                case "irrfan_khan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.irrfan_khan_icon);
                    break;
                case "jackie_chan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jackie_chan_icon);
                    break;
                case "james_harden_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.james_harden_icon);
                    break;
                case "james_patterson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.james_patterson_icon);
                    break;
                case "jay_z_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jay_z_icon);
                    break;
                case "jerry_einfeld_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jerry_einfeld_icon);
                    break;
                case "jimmy_buffett_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jimmy_buffett_icon);
                    break;
                case "jordan_pieth_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.jordan_pieth_icon);
                    break;
                case "justin_bieber_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.justin_bieber_icon);
                    break;
                case "karan_johar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.karan_johar_icon);
                    break;
                case "kei_nishikori_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kei_nishikori_icon);
                    break;
                case "kenny_chesney_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kenny_chesney_icon);
                    break;
                case "kevin_durant_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kevin_durant_icon);
                    break;
                case "kevin_hary_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kevin_hary_icon);
                    break;
                case "lebron_james_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.lebron_james_icon);
                    break;
                case "lewis_hamilton_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.lewis_hamilton_icon);
                    break;
                case "lionel_messi_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.lionel_messi_icon);
                    break;
                case "louis_c_k_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.louis_c_k_icon);
                    break;
                case "luke_bryan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.luke_bryan_icon);
                    break;
                case "mahesh_babu_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mahesh_babu_icon);
                    break;
                case "mark_wahlberg_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.mark_wahlberg_icon);
                    break;
                case "metallica_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.metallica_icon);
                    break;
                case "ms_dhoni_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ms_dhoni_icon);
                    break;
                case "neymar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.neymar_icon);
                    break;
                case "novak_djokovic_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.novak_djokovic_icon);
                    break;
                case "paul_mccartney_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.paul_mccartney_icon);
                    break;
                case "phil_mcgraw_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.phil_mcgraw_icon);
                    break;
                case "phil_mickelson_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.phil_mickelson_icon);
                    break;
                case "prabhas_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.prabhas_icon);
                    break;
                case "ravichandran_ashwin_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ravichandran_ashwin_icon);
                    break;
                case "ravindra_jadeja_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ravindra_jadeja_icon);
                    break;
                case "red_hot_chili_peppers_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.red_hot_chili_peppers_icon);
                    break;
                case "robert_downey_jr_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.robert_downey_jr_icon);
                    break;
                case "roger_federer_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.roger_federer_icon);
                    break;
                case "rory_mcilroy_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rory_mcilroy_icon);
                    break;
                case "rush_limbaugh_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.rush_limbaugh_icon);
                    break;
                case "russell_westbrook_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.russell_westbrook_icon);
                    break;
                case "ryan_seacrest_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.ryan_seacrest_icon);
                    break;
                case "s_s_rajamouli_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.s_s_rajamouli_icon);
                    break;
                case "sachin_tendulkar_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sachin_tendulkar_icon);
                    break;
                case "salman_khan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.salman_khan_icon);
                    break;
                case "sean_hannity_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sean_hannity_icon);
                    break;
                case "sebastian_vettel_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.sebastian_vettel_icon);
                    break;
                case "shah_rukh_khan_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.shah_rukh_khan_icon);
                    break;
                case "simon_cowell_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.simon_cowell_icon);
                    break;
                case "steve_harvey_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.steve_harvey_icon);
                    break;
                case "the_chainsmokers_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.the_chainsmokers_icon);
                    break;
                case "the_weeknd_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.the_weeknd_icon);
                    break;
                case "tiesto_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tiesto_icon);
                    break;
                case "tiger_woods_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tiger_woods_icon);
                    break;
                case "toby_keith_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.toby_keith_icon);
                    break;
                case "tom_cruise_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tom_cruise_icon);
                    break;
                case "usain_bolt_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.usain_bolt_icon);
                    break;
                case "vin_diesel_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.vin_diesel_icon);
                    break;
                case "virat_kohli_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.virat_kohli_icon);
                    break;
                case "zlatan_ibrahimovic_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.zlatan_ibrahimovic_icon);
                    break;
            }
        }
    }
}
