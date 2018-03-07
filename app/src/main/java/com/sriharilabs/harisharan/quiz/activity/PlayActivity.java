package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.database.DatabaseHandler;
import com.sriharilabs.harisharan.quiz.database.QuestionBean;
import com.sriharilabs.harisharan.quiz.databinding.ActivityPlayBinding;
import com.sriharilabs.harisharan.quiz.utill.Constant;
import com.sriharilabs.harisharan.quiz.utill.PreferenceConnector;

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
    private final int TOTAL_QUESTION = 18;

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
        initTimer(); // Set time
        getAllQuestion(); // Get all question list
        makeQuestion(); // Make new question
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
            openCongratulationScreen();
            makeQuestion();
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
        DatabaseHandler db = new DatabaseHandler(this);
        questionList = db.getAllContacts();
        if (questionList.size() > 0) {
            Collections.shuffle(questionList);
        }
    }

    /**
     * Make question
     */
    private void makeQuestion() {
        openCongratulationScreen();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed()) {
                    return;
                }
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
        String answer = questionList.get(questionNumber).getAnswer();
        String selectType = questionList.get(questionNumber).getType();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++) {
            String type = questionList.get(i).getType();
            if (type.equalsIgnoreCase(selectType) &&
                    !answer.equalsIgnoreCase(questionList.get(i).getAnswer())) {
                list.add(questionList.get(i).getAnswer());
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
                checkGameOver();
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
                    mBinding.imageViewIcon.setImageResource(R.drawable.adidas_icon);
                    break;
                case "abn_amro_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.adobe_icon);
                    break;
                case "accenture_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.airbnb_icon);
                    break;
                case "adidas_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.american_airlines_icon);
                    break;
                case "adobe_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.applied_materials_icon);
                    break;
                case "adp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.banco_do_brasil_icon);
                    break;
                case "agricultural-bank_of_china_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.credit_agricole_icon);
                    break;
                case "aia_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.daikin_icon);
                    break;
                case "airbnb_icon":
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
                case "american_airlines_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.american_airlines_icon);
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
                    mBinding.imageViewIcon.setImageResource(R.drawable.bank_of_Communications_icon);
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
                case "dove_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dove_icon);
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
            }
        }
    }
}
