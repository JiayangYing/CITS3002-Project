package com.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Session {
    private ArrayList<Question> question;
    private boolean isLogin;
    private String userID;
    private boolean examFinish;
    private int qstIdx; // question current index

    public Session(String user) {
        userID = user;
        question = new ArrayList<>();
        isLogin = false;
        examFinish = false;
        Random random = new Random();
        // create hashmap to make sure the unique question id is selected
        HashMap<Integer, Integer> uniqueSeq = new HashMap<>();
        HashMap<Integer, Integer> getUniq = new HashMap<>();
        int idx = 0;
        while (uniqueSeq.size() < 10) {
            int id = random.nextInt(11);
            if (!uniqueSeq.containsKey(id)) {
                uniqueSeq.put(id, id);
                getUniq.put(idx, id);
                idx++;
            }
        }
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                question.add(new Question());
                question.get(i).setqID(String.valueOf(getUniq.get(i)));
                question.get(i).setIsMCQ(true);
                question.get(i).setQuestionLang("java");
            } else if (i < 9) {
                question.add(new Question());
                question.get(i).setqID(String.valueOf(getUniq.get(i)));
                question.get(i).setIsMCQ(true);
                question.get(i).setQuestionLang("python");
            }
        }
        // the last one will be the coding challenge
        question.add(new Question());
        int codeIdx = random.nextInt(2);
        int codeLang = random.nextInt(2);
        if (codeLang == 0) {
            question.get(9).setQuestionLang("python");
        } else {
            question.get(9).setQuestionLang("java");
        }
        question.get(9).setqID(String.valueOf(codeIdx));
        question.get(9).setIsMCQ(false);
        qstIdx = 0;
    }

    public void setLogin(boolean tf) {
        isLogin = tf;
    }

    // check if the test is completed
    public boolean isFinish() {
        boolean finish = true;
        for (int i = 0; i < question.size(); i++) {
            if (!question.get(i).isDone()) {
                finish = false;
            }
        }
        return finish;
    }


    // calculate the global mark
    public String calc_mark() {
        String init = "--";
        int mark = 0;
        for (int i = 0; i < question.size(); i++) {
            if (question.get(i).isDone()) {
                init = "";
                mark += question.get(i).returnMark();
            }
        }
        if (init == "--") {
            return init;
        }
        return String.valueOf(mark);


    }

    public ArrayList<Question> getQuestion() {
        return question;
    }

    //set the currently focusing question
    public void setQstIdx(int i) {
        this.qstIdx = i;
    }

    // get the currently focusing question index
    public int getQstIdx() {
        return this.qstIdx;
    }
}
