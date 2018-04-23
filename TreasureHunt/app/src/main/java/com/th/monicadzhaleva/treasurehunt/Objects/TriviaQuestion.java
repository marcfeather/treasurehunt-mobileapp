package com.th.monicadzhaleva.treasurehunt.Objects;

/**
 * Created by monicadzhaleva on 14/03/2018.
 */

public class TriviaQuestion {
    String question;
    String answer1;
    String answer2;
    String answer3;
    String answer4;
    String answer_correct;

    public TriviaQuestion()
    {
    }

    public TriviaQuestion(String question, String answer1, String answer2, String answer3, String answer4, String answer_correct) {
        this.question = question;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.answer4 = answer4;
        this.answer_correct = answer_correct;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer1() {
        return answer1;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getAnswer3() {
        return answer3;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public String getAnswer4() {
        return answer4;
    }

    public void setAnswer4(String answer4) {
        this.answer4 = answer4;
    }

    public String getCorrect_answer() {
        return answer_correct;
    }

    public void setCorrect_answer(String answer_correct) {
        this.answer_correct = answer_correct;
    }
}
