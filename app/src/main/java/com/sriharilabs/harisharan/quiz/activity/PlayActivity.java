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
     * Set image
     */
    private void setImage() {
        String image = questionList.get(questionNumber).getImage();
        if (!TextUtils.isEmpty(image)) {
            switch (image) {
                case "abbott_labs_icon": // Abbott Labs
                    mBinding.imageViewIcon.setImageResource(R.drawable.abbott_labs_icon);
                    break;
                case "adidas_icon": // Adidas
                    mBinding.imageViewIcon.setImageResource(R.drawable.adidas_icon);
                    break;
                case "adobe_icon": // Adobe
                    mBinding.imageViewIcon.setImageResource(R.drawable.adobe_icon);
                    break;
                case "airbnb_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.airbnb_icon);
                    break;
                case "american_airlines_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.american_airlines_icon);
                    break;
                case "applied_materials_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.applied_materials_icon);
                    break;
                case "banco_do_brasil_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.banco_do_brasil_icon);
                    break;
                case "credit_agricole_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.credit_agricole_icon);
                    break;
                case "daikin_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.daikin_icon);
                    break;
                case "dove_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.dove_icon);
                    break;
                case "amit_ji_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.amit_ji_icon);
                    break;
                case "shardha_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.shardha_icon);
                    break;
                case "akki_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.akki_icon);
                    break;
                case "disha_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.disha_icon);
                    break;
                case "tom_cruise_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.tom_cruise_icon);
                    break;
                case "johnny_depp_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.johnny_depp_icon);
                    break;
                case "kristen_stewart_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.kristen_stewart_icon);
                    break;
                case "angelina_jolie_icon":
                    mBinding.imageViewIcon.setImageResource(R.drawable.angelina_jolie_icon);
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
}
