package com.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.cert.CRL;

public class ResponseHandler {
    private BufferedWriter buffWtr;
    private StringBuilder content;
    private StringBuilder headInfo;
    private int len;
    private final String BLANK = " ";
    private final String CRLF = "\r\n";

    public ResponseHandler(Socket socket){
        try {
            content = new StringBuilder();
            headInfo = new StringBuilder();
            len = 0;
            buffWtr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // add content and the length value
    public ResponseHandler print(String contnt) {
        this.content.append(contnt);
        len += contnt.getBytes(StandardCharsets.UTF_8).length;
        return this;
    }

    // add content and the length value
    public ResponseHandler println(String contnt) {
        this.content.append(contnt).append(CRLF);
        len += (contnt + CRLF).getBytes(StandardCharsets.UTF_8).length;
        return this;
    }

    // combine the header and content and send to the browser
    public void pushToBrowser(int status, boolean isLogout) throws IOException {
        if (null == headInfo){
            status = 505;
        }
        createHeadInfo(status, isLogout);
        buffWtr.append(headInfo);
        buffWtr.append(content);
        buffWtr.flush();
        buffWtr.close();
    }

    // create response header information
    private void createHeadInfo(int status, boolean isLogoff) {
        headInfo.append("HTTP/1.1").append(BLANK);
        headInfo.append(status).append(BLANK);
        switch (status){
            case 200:
                headInfo.append("OK").append(CRLF);
                break;
            case 403:
                headInfo.append("FORBIDDEN").append(CRLF);
                break;
            case 404:
                headInfo.append("NOT FOUND").append(CRLF);
                break;
            case 505:
                headInfo.append("SERVER ERROR").append(CRLF);
        }
        headInfo.append("Content-type: text/html; charset=UTF-8").append(CRLF);
        headInfo.append("Content-Length: ").append(len).append(CRLF);
        headInfo.append(CRLF);
    }
}
