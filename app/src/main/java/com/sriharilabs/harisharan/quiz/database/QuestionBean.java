package com.sriharilabs.harisharan.quiz.database;

public class QuestionBean {
    private int id=0;
    private String answer = "";
    private String image = "";
    private String type = "";

    public QuestionBean() {
    }

    public QuestionBean(int id, String answer, String image, String type) {
        this.id = id;
        this.answer = answer;
        this.image = image;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}