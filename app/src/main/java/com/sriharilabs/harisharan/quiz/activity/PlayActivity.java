package com.sriharilabs.harisharan.quiz.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.database.DatabaseHandler;
import com.sriharilabs.harisharan.quiz.database.QuestionBean;
import com.sriharilabs.harisharan.quiz.databinding.ActivityPlayBinding;
import com.sriharilabs.harisharan.quiz.utill.Constant;
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
                break;
            case 2: // B
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
                //checkGameOver();
                countDownTimer.cancel();
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
            makeQuestion();
            countDownTimer.start();
        }
    }

    /**
     * Open game over screen
     */
    private void openGameOverScreen() {
        Intent intent = new Intent(PlayActivity.this, GameOverActivity.class);
        intent.putExtra("point", POINT + "");
        startActivity(intent);
        finish();
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
        questionNumber++;
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
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++) {
            String type = questionList.get(i).getType();
            if (type.equalsIgnoreCase(Constant.TYPE_LOGO) &&
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
            }
        }
    }
}
