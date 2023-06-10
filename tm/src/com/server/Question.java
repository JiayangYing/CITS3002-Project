package com.server;

public class Question {
    private String qID; // question id from the QB
    private boolean isMCQ; // whether it is a multiple choice question
    private int attempts = 3; // remaining attempts
    private int mark = 3; // current question mark
    private boolean isDone = false; // is finished or not
    private String questionLang; // question language type


    public void setqID(String id){this.qID = id;}// set id
    public void addAttempt(){this.attempts--;} // reduce the attempt remaining
    public void lossMark(){this.mark--;} // reduce the mark
    public void setIsMCQ(boolean is) {this.isMCQ = is;} // set the question whether it is a mcq
    public void setDone() {this.isDone = true;} // set the question finished
    // set the question language type
    public void setQuestionLang(String language) {this.questionLang = language.toLowerCase();}
    public String getQuestionLang() {return this.questionLang;} // return the language type
    public boolean canAttempt() {return attempts > 0 && !isDone;} // check whether it can still attempt
    public int attemptRem() {return attempts;} // return remaining attempt
    public boolean isDone() {return isDone;} // whether the question is done check
    public boolean haveMark(){return mark >= 0;} // question still have mark
    public int returnMark() {return this.mark;} // return the mark
    public String getqID(){return this.qID;} // get the current question id
    public boolean isMCQ() {return this.isMCQ;} // check whether the question is a mcq
}
