package com.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;


public class UserConnectHandler {
    private ResponseHandler response; // http response handler
    private RequestHandler request; // http request handler
    private HashMap<String, Session> sessioner; // user session handler
    private Socket currentClnt; // current user socket
    private QbRequester pyQB; // qb requester socket for python
    private QbRequester javaQB; // qb requester socket for java
    private HTMLGenerator webPrinter; // html content generator
    private Authenticator auth; // user authenticator


    public UserConnectHandler(Socket socket, HashMap<String, Session> session,
                              Authenticator auth, QbRequester pyqb, QbRequester javaqb) {
        currentClnt = socket;
        sessioner = session;
        this.auth = auth;
        this.webPrinter = new HTMLGenerator();
        this.pyQB = pyqb;
        this.javaQB = javaqb;
    }

    public void process() {
        // initialize the request and response
        try {
            this.request = new RequestHandler(currentClnt);
        } catch (IOException e) {
            System.err.println(e);
        }
        this.response = new ResponseHandler(currentClnt);
        // handle the first visit
        if (request.getMethod().equalsIgnoreCase("GET") &&
                (request.getPath().equals("/") || request.getPath().equals("/login"))) {
            response.println(webPrinter.loginBuilder());
            try {
                response.pushToBrowser(200, false);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        // handle the login value to authenticate
        if (request.getMethod().equalsIgnoreCase("POST") && request.getPath().equalsIgnoreCase("/homepage")) {
            // if it is visited from login
            if (request.getParameters().containsKey("type") && request.getValue("type").equals("login")) {
                // authenticate
                if (auth.authenticate(request.getValue("username"), request.getValue("password"))) {
                    String currentUser = request.getValue("username");
                    // if is fist initialise
                    if (sessioner.containsKey(currentUser)) {
                        System.out.println("Settled user login");
                    }
                    // create new session for new user
                    if (!sessioner.containsKey(currentUser)) {
                        sessioner.put(currentUser, new Session(currentUser));
                        sessioner.get(currentUser).setLogin(true);
                    }
                    String mode;
                    String mark;
                    // display the mark and if all the question are finished, change the Start button to View
                    if (sessioner.get(currentUser).isFinish()) {
                        mode = "view";
                        mark = sessioner.get(currentUser).calc_mark();
                    } else {
                        mode = "test";
                        mark = sessioner.get(currentUser).calc_mark();
                    }
                    // generate the html content
                    String content = webPrinter.homepageBuilder(mode, currentUser, mark);
                    response.println(content);
                    try {
                        response.pushToBrowser(200, false);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                } else {
                    // if the user and password not match
                    response.println("Wrong username or password");
                    try {
                        response.pushToBrowser(403, false);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }
            // if is returned from test
            if (request.getPath().equalsIgnoreCase("/homepage") && request.getValue("type").equals("backhome")) {
                String currentUser = request.getValue("user");
                // if is fist initialise
                if (sessioner.containsKey(currentUser)) {
                    System.out.println("Settled user login");
                }
                if (!sessioner.containsKey(currentUser)) {
                    sessioner.put(currentUser, new Session(currentUser));
                    sessioner.get(currentUser).setLogin(true);
                }
                String mode;
                String mark;
                if (sessioner.get(currentUser).isFinish()) {
                    mode = "view";
                    mark = sessioner.get(currentUser).calc_mark();
                } else {
                    mode = "test";
                    mark = sessioner.get(currentUser).calc_mark();
                }
                String content = webPrinter.homepageBuilder(mode, currentUser, mark);
                response.println(content);
                try {
                    response.pushToBrowser(200, false);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
        // this if statement is the main part of test perform
        if (request.getMethod().equalsIgnoreCase("POST") && request.getPath().equalsIgnoreCase("/test")) {
            // Match the current visiter
            String currentUser = request.getValue("user");
            // match the session for the user
            Session currentSession = sessioner.get(currentUser);
            // get current visiting question index
            int qIdx = currentSession.getQstIdx();
            // manipulate the page changing
            if (request.getValue("type").equals("previous")) {
                qIdx--;
                if (qIdx < 0){
                    qIdx = 0;
                }
                currentSession.setQstIdx(qIdx);
            } else if (request.getValue("type").equals("next")) {
                qIdx++;
                if (qIdx > 9){
                    qIdx = 9;
                }
                currentSession.setQstIdx(qIdx);
            }
            // get current question
            Question currQus = currentSession.getQuestion().get(qIdx);
            // get current question's attempt time remain and question type
            int canAttempt = currQus.attemptRem();
            String qType;
            if (currQus.isMCQ()) {
                qType = "choice";
            } else {
                qType = "code";
            }
            // set the question language type
            String qLang;
            if(currQus.getQuestionLang().equals("python"))
                qLang = "python";
            else{
                qLang = "java";
            }
            // wait for qb implement
            String qtxt = "Wait for QB";
            // get the question content from qb
            try {
                if (currQus.isMCQ()) {
                    if(qLang.equals("python")){
                        qtxt = pyQB.questionText(currQus.getqID());
                    }
                    else{
                        qtxt = javaQB.questionText(currQus.getqID());
                    }
                } else {
                    // if it is a coding question
                    if (qLang.equals("python")){
                        qtxt = pyQB.getProgram(currQus.getqID());
                    }else{
                        qtxt = javaQB.getProgram(currQus.getqID());
                    }
                }

            } catch (IOException e) {
                String content = "Something wring with QB";
                response.println(content);
                try {
                    response.pushToBrowser(403, false);
                } catch (IOException ex) {
                    System.err.println("QB error html send error: "+ex);
                }
                System.err.println(e);
            }
            // handle the answer submission
            if (request.getValue("type").equals("submit")) {
                if (qType.equals("choice")) {
                    // get user input answer
                    String mcqAnswer;
                    if (request.getValue("choice") != null) {
                        mcqAnswer = request.getValue("choice");
                    } else {
                        mcqAnswer = "";
                    }
                    // need QB implement
                    String checkAns = "A";
                    // get answer from qb
                    try {
                        if (qLang.equals("python")){
                            checkAns = pyQB.questionAnswer(currQus.getqID());
                        }else{
                            checkAns = javaQB.questionAnswer(currQus.getqID());
                        }

                    } catch (IOException e) {
                        String content = "Something wring with QB";
                        response.println(content);
                        try {
                            response.pushToBrowser(403, false);
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                        System.err.println("get MCQ answer error");
                    }
                    // this part could use qb to check answer
                    if (mcqAnswer.equalsIgnoreCase(checkAns)) {
                        // set done if the question is done
                        currQus.setDone();
                        // prepare the html content
                        String content = webPrinter.questionViewBuilder(
                                String.valueOf(currQus.returnMark()), checkAns, qIdx + 1, qtxt, currentUser);
                        try {
                            // push it in and send to browser
                            response.println(content);
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println("test submit mcq first attempt correct error");
                            System.err.println(e);
                        }
                    } else {
                        // if not correct first time
                        currQus.addAttempt();
                        currQus.lossMark();
                        // check whether it still attempt-able
                        if (!currQus.canAttempt()) {
                            currQus.setDone();
                        }
                        // if still have chance
                        if (!currQus.isDone()) {
                            String content = webPrinter.questionAttemptBuilder(qType, qIdx + 1, qtxt,
                                    String.valueOf(currQus.attemptRem()), currentUser, 2, null, null);
                            response.println(content);
                            try {
                                response.pushToBrowser(200, false);
                            } catch (IOException e) {
                                System.err.println("test submit mcq can attempt but not right error");
                                System.err.println(e);
                            }
                        }
                        // if no chance
                        else {
                            String content = webPrinter.questionViewBuilder(
                                    String.valueOf(currQus.returnMark()), checkAns, qIdx + 1, qtxt, currentUser);
                            try {
                                response.println(content);
                                response.pushToBrowser(200, false);
                            } catch (IOException e) {
                                System.err.println("test submit mcq 0 attempt error");
                                System.err.println(e);
                            }
                        }
                    }
                } else {
                    // process of doing programming challenge
                    String codeTxt;
                    if (request.getValue("code") != null) {
                        codeTxt = request.getValue("code");
                    } else {
                        codeTxt = "";
                    }

                    // need qb to run the code
                    // string array [boolean for true or not,
                    //               get result,
                    //               expect result]
                    String[] result = {"1", "22", "33"};
                    try {
                        if (qLang.equals("python")){
                            result = pyQB.postProgram(currQus.getqID(), codeTxt);
                        }else{
                            result = javaQB.postProgram(currQus.getqID(), codeTxt);
                        }

                    } catch (IOException e) {
                        String content = "Something wring with QB";
                        response.println(content);
                        try {
                            response.pushToBrowser(403, false);
                        } catch (IOException ex) {
                            System.err.println("Get code text error"+ex);
                        }
                        System.err.println("process code error");
                    }
                    // if it is correct
                    if (result[0].equals("1")) {
                        currQus.setDone();
                        String mark = String.valueOf(currQus.returnMark());
                        String content = webPrinter.questionAttemptBuilder(qType,
                                qIdx + 1, qtxt, "0", currentUser,
                                1, result[2], mark);
                        try {
                            response.println(content);
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println("test submit code first attempt error");
                            System.err.println(e);
                        }
                    }
                    // if it is not correct
                    else {
                        currQus.addAttempt();
                        currQus.lossMark();
                        // check if the question could still do
                        if (!currQus.canAttempt()) {
                            currQus.setDone();
                        }
                        // if still have chance
                        if (!currQus.isDone()) {
                            String mark = String.valueOf(currQus.returnMark());
                            String content = webPrinter.questionAttemptBuilder(qType,
                                    qIdx + 1, qtxt, String.valueOf(currQus.attemptRem()),
                                    currentUser, 2, result[1] + " expect " + result[2], mark);
                            response.println(content);
                            try {
                                response.pushToBrowser(200, false);
                            } catch (IOException e) {
                                System.err.println("Test sumbit code can attempt but not right error!");
                                System.err.println(e);
                            }
                        }
                        // if the question have no chance
                        else {
                            String content = webPrinter.questionViewBuilder(
                                    String.valueOf(currQus.returnMark()), result[1] + " expect " + result[2],
                                    qIdx + 1, qtxt, currentUser);
                            try {
                                response.println(content);
                                response.pushToBrowser(200, false);
                            } catch (IOException e) {
                                System.err.println("code test submit mcq 0 attempt error");
                                System.err.println(e);
                            }
                        }
                    }
                }
            }
            // if the button type is not submit, then do the normal navigation
            if (request.getValue("type").equalsIgnoreCase("test") ||
                    request.getValue("type").equalsIgnoreCase("previous") ||
                    request.getValue("type").equalsIgnoreCase("next")) {
                // if question have no chance
                if (currQus.isDone()) {
                    // if it is a mcq
                    if (currQus.isMCQ()) {
                        // need to implement qb
                        String answer = "need to be query";
                        try {
                            if (qLang.equals("python")){
                                answer = pyQB.questionAnswer(currQus.getqID());
                            }else{
                                answer = javaQB.questionAnswer(currQus.getqID());
                            }

                        } catch (IOException e) {
                            String content = "Something wring with QB";
                            response.println(content);
                            try {
                                response.pushToBrowser(403, false);
                            } catch (IOException ex) {
                                System.err.println("get view ans qb error and push out err: "+ex);
                            }
                            System.err.println("get view answer error" + e);
                        }
                        // String qTxt = "This need to be query";
                        String content = webPrinter.questionViewBuilder(String.valueOf(currQus.returnMark()),
                                answer, qIdx + 1, qtxt, currentUser);
                        response.println(content);
                        try {
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    }
                    // if it is a coding challenge
                    else {
                        String answer = "need to be q";
                        // get question from qb
                        try {
                            if (qLang.equals("python")){
                                answer = pyQB.getProgAns(currQus.getqID());
                            }else{
                                answer = javaQB.getProgAns(currQus.getqID());
                            }

                        } catch (IOException e) {
                            String content = "Get code text error";
                            response.println(content);
                            try {
                                response.pushToBrowser(403, false);
                            } catch (IOException ex) {
                                System.err.println("push out err: " + ex);
                            }
                            System.out.println("get view code answer error.");
                        }
                        // generate the html content
                        String content = webPrinter.questionViewBuilder(
                                String.valueOf(currQus.returnMark()),
                                answer, qIdx + 1, qtxt, currentUser);
                        response.println(content);
                        try {
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println("code q view error");
                            System.err.println(e);
                        }
                    }
                    //if the question have chance
                } else if (!currQus.isDone()) {
                    if (currQus.isMCQ()) {
                        // String answer = "need to be query";
                        String content = webPrinter.questionAttemptBuilder(qType, qIdx + 1, qtxt,
                                String.valueOf(currQus.attemptRem()), currentUser, 0, null, null);
                        response.println(content);
                        try {
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    }
                    // doing the coding challenge
                    else {
                        String content = webPrinter.questionAttemptBuilder(qType, qIdx + 1, qtxt,
                                String.valueOf(currQus.attemptRem()), currentUser, 0, null, null);
                        response.println(content);
                        try {
                            response.pushToBrowser(200, false);
                        } catch (IOException e) {
                            System.err.println("code still can try view error");
                            System.err.println(e);
                        }
                    }
                } else {
                    System.err.println("System don't know what to do!");
                }
            } else {
                System.out.println("something wrong with the test page" + "type = " + request.getValue("type"));
            }
        }
        // just single logoff page
        else if (request.getMethod().equalsIgnoreCase("POST") && request.getValue("type").equals("logoff")) {
            String content = "You've logoff!";
            response.println(content);
            try {
                response.pushToBrowser(200, true);
            } catch (IOException e) {
                System.err.println("logoff error");
                System.err.println(e);
            }
        }
    }
}
