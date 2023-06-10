package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
/*
The Syntax of this protocol community to QB is:
QueryType=type&QueryID=id&ansSubmit=submit
We use "&" as delimiter and use "=" to perform type=value
And at the beginning of the query we generate a 10 index length to show the query byte length
Which is "Len=xx    "
 */

// This class is handling the strategy for QB request
public class QbRequester {
    // a socket that point to the connection between qb and tm
    private Socket socket;
    // Stream output
    private PrintWriter out;
    // Stream input
    private BufferedReader in;

    public QbRequester(Socket socket) throws IOException {
        // store the socket
        this.socket = socket;
        // initialise the reader and writer
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    // query for ask mcq question content
    public String questionText(String queryID) throws IOException {

        return sendRequest("questionText", queryID, null);
    }

    // query for ask mcq question answer
    public String questionAnswer(String queryID) throws IOException {
        return sendRequest("questionAnswer", queryID, null);
    }

    // check mcq answer
    public boolean ansCheck(String queryID, String ansSubmit) throws IOException {
        return Boolean.parseBoolean(sendRequest("ansCheck", queryID, ansSubmit));
    }

    // query for get the coding challenge content
    public String getProgram(String queryID) throws IOException {
        return sendRequest("getProgram", queryID, null);
    }

    // query for get the answer of coding
    public String getProgAns(String queryID) throws IOException {
        return sendRequest("getProgAns", queryID, null);
    }

    // send the question code and get the executed result
    public String[] postProgram(String queryID,String content) throws IOException {
        String response = sendRequest("postProgram", queryID, content);
        String[] rspArr = response.split("&");
        return rspArr;
    }

    // close the connection
    public void close() throws IOException {
        sendRequest("close", null, null);
        in.close();
        out.close();
        socket.close();
    }

    // add the content length at the beginning (10 index long)
    private StringBuilder buildRequest(StringBuilder requestData) {
        int length = requestData.length(); // Adding 10 for "len=X" with padding
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("len=");
        requestBuilder.append(String.format("%-6d", length));
        requestBuilder.append(requestData);
        return requestBuilder;
    }

    // the main method to interact with QB
    private String sendRequest(String queryType, String queryID, String ansSubmit) throws IOException {
        String encodedQueryType = URLEncoder.encode(queryType, "UTF-8");
        StringBuilder requestData = new StringBuilder("queryType=" + encodedQueryType);

        if (queryID != null) {
            String encodedQueryID = URLEncoder.encode(queryID, "UTF-8");
            requestData.append("&queryID=").append(encodedQueryID);
        }

        if (ansSubmit != null) {
            String encodedAnsSubmit = URLEncoder.encode(ansSubmit, "UTF-8");
            requestData.append("&ansSubmit=").append(encodedAnsSubmit);
        }
        requestData = buildRequest(requestData);
        out.print(requestData);
        out.flush();
        
        // after send data
        // Read the first 10 characters for the length string
        char[] lengthBuffer = new char[10];
        int charsRead = in.read(lengthBuffer, 0, 10);
        if (charsRead != 10) {
            return "Error: Failed to read the message.";
        }

        String lengthString = new String(lengthBuffer);

        // Parse the length string to get the remaining data length
        int dataLength = Integer.parseInt(lengthString.substring(4).trim());

        // Use a custom buffered receiver to read the remaining data
        char[] dataBuffer = new char[dataLength];
        int totalCharsRead = 0;
        while (totalCharsRead < dataLength) {
            charsRead = in.read(dataBuffer, totalCharsRead, dataLength - totalCharsRead);
            if (charsRead == -1) {
                break;
            }
            totalCharsRead += charsRead;
        }

        if (totalCharsRead != dataLength) {
            return "Error: Failed to read the message.";
        }

        String responseData = new String(dataBuffer);
        System.out.println(responseData);
        return responseData;
    }
}