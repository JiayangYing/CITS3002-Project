package com.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class HTMLGenerator {
    // path for four html template
    private Path questionPath;
    private Path homepagePath;
    private Path loginPath;
    private Path logoffPath;

    public HTMLGenerator() {
        this.questionPath = Paths.get("./Template/Question_Template.html");
        this.homepagePath = Paths.get("./Template/homepage.html");
        this.loginPath = Paths.get("./Template/login.html");
        this.logoffPath = Paths.get("./Template/Logoff.html");
    }

    // type is question type, Idx is question index, content is question content
    // attempt is the amount of attempt remaining, user is current user pointer
    // result is a check key, 0 is just view the question, 1 is correct, 2 is wrong
    // answer is the answer get from qb, mark is the question mark
    public String questionAttemptBuilder(String type, int Idx, String content, String attempt,
                                         String user, int result, String answer, String mark) {
        // read html into memory
        String htmlContent = null;
        try {
            htmlContent = new String(Files.readAllBytes(questionPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // replace the associated value
        htmlContent = htmlContent.replace("{attempt_t}", attempt);
        htmlContent = htmlContent.replace("{question_index}", String.valueOf(Idx));
        htmlContent = htmlContent.replace("{question_text}", content);
        // htmlContent = htmlContent.replace("{answer_style}", "");
        htmlContent = htmlContent.replace("{user_id}", user);
        // management for mcq question type
        if (type.equalsIgnoreCase("choice")) {
            htmlContent = htmlContent.replace("{option_style}", "");
            htmlContent = htmlContent.replace("{code_style}", "display: none");
        } else {
            // management for code question type
            htmlContent = htmlContent.replace("{option_style}", "display: none");
            htmlContent = htmlContent.replace("{code_style}", "");
        }
        // if it is not answer checking state
        if (result == 0){
            htmlContent = htmlContent.replace("{answer_style}", "");
            htmlContent = htmlContent.replace("{result_s_sty}", "display: none");
            htmlContent = htmlContent.replace("{correct_answer_style}", "display: none");
        } // if it is correct answer result
        else if (result == 1) {
            htmlContent = htmlContent.replace("{answer_style}", "display: none");
            htmlContent = htmlContent.replace("{result_s_sty}", "");
            htmlContent = htmlContent.replace("{result_state}", "Correct, you can do next");
            htmlContent = htmlContent.replace("{correct_answer_style}", "");
            if (null != answer) {
                htmlContent = htmlContent.replace("{correct_answer_style}", "");
                htmlContent = htmlContent.replace("{correct_answer}", answer);
                htmlContent = htmlContent.replace("{mark_holder}", mark);
            }
            // if it is wrong result for coding challenge
        } else if (result == 2){
            htmlContent = htmlContent.replace("{answer_style}", "");
            htmlContent = htmlContent.replace("{result_s_sty}", "");
            htmlContent = htmlContent.replace("{result_state}", "Wrong");
            if (type.equals("code")){
                htmlContent = htmlContent.replace("{correct_answer_style}", "");
                htmlContent = htmlContent.replace("{correct_answer}", answer);
                htmlContent = htmlContent.replace("{mark_holder}", mark);
            }
            else {
                htmlContent = htmlContent.replace("{correct_answer_style}", "display: none");
            }
        }
        // htmlContent = htmlContent.replace("{correct_answer_style}", "display: none");
        // modify the page changing button if they reach to the edge
        if (Idx == 1) {
            htmlContent = htmlContent.replace("{prev_button_style}", "display: none");
        } else{
            htmlContent = htmlContent.replace("{prev_button_style}", "");
        }
        if (Idx == 10) {
            htmlContent = htmlContent.replace("{next_button_style}", "display: none");
        }else{
            htmlContent = htmlContent.replace("{next_button_style}", "");
        }
        return htmlContent;
    }

    // This method is just for question view, can not answer
    // mark is question mark, idx is question id pointer
    // content is question text, user is for hidden id checker
    public String questionViewBuilder(String mark, String answer, int Idx, String content, String user){
        String htmlContent = null;
        try {
            htmlContent = new String(Files.readAllBytes(questionPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        htmlContent = htmlContent.replace("{attempt_t}", "0");
        htmlContent = htmlContent.replace("{user_id}", user);
        htmlContent = htmlContent.replace("{question_index}", String.valueOf(Idx));
        htmlContent = htmlContent.replace("{question_text}", content);
        htmlContent = htmlContent.replace("{answer_style}", "display: none");
        htmlContent = htmlContent.replace("{correct_answer_style}", "");
        htmlContent = htmlContent.replace("{result_s_sty}", "display: none");
        htmlContent = htmlContent.replace("{correct_answer}", answer);
        htmlContent = htmlContent.replace("{mark_holder}", mark);
        if (Idx == 1) {
            htmlContent = htmlContent.replace("{prev_button_style}", "display: none");
        } else {
            htmlContent = htmlContent.replace("{prev_button_style}", "");
        }
        if (Idx == 10) {
            htmlContent = htmlContent.replace("{next_button_style}", "display: none");
        } else {
            htmlContent = htmlContent.replace("{next_button_style}", "");
        }
        return htmlContent;
    }


    // generate the homepage
    public String homepageBuilder(String mode, String user, String mark) {
        try {
            String htmlContent = new String(Files.readAllBytes(homepagePath));
            if (null != user) {
                htmlContent = htmlContent.replace("{user_id}", user);
                htmlContent = htmlContent.replace("{user_holder}", user);
            } else {
                htmlContent = htmlContent.replace("{user_holder}", "null");
            }
            if (null != mark) {
                htmlContent = htmlContent.replace("{global_mark_holder}", mark);
            } else {
                htmlContent = htmlContent.replace("{global_mark_holder}", "--");
            }
            if (mode.equalsIgnoreCase("view")) {
                htmlContent = htmlContent.replace("{button1_value}", "test");
                htmlContent = htmlContent.replace("{button1_text}", "View");
            } else {
                htmlContent = htmlContent.replace("{button1_value}", "test");
                htmlContent = htmlContent.replace("{button1_text}", "Start");
            }
            return htmlContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // generate the login page
    public String loginBuilder() {
        String htmlContent = null;
        try {
            htmlContent = new String(Files.readAllBytes(loginPath));
            return htmlContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String logoutBuilder() {
        String htmlContent = null;
        try {
            htmlContent = new String(Files.readAllBytes(logoffPath));
            return htmlContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
