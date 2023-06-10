package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/*
This class is for handling the HTTP protocol information manipulation
get the request method and path
and get the request header information
 */
public class RequestHandler {
    private String method; // http request method get/post
    private String path; // http requesting path
    private HashMap<String, String> headers; // the http header information
    private HashMap<String, String> parameters; // if it is post, it is the parameter
    private boolean hasContent;

    public RequestHandler(Socket socket) throws IOException{
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        handleRequest(reader);
    }

    // get method, path, and header information
    private void handleRequest(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            System.out.println("Request handles nothing");
            method = "";
            path = "";
            return;
        }
        // analyse the first line
        String[] requestLine = line.split(" ");
        try {
            method = requestLine[0].toUpperCase();
            path = requestLine[1];
        }
        catch (Exception e){
            System.err.println("error in read method/path " + e);
        }
        // analyse the header and store in local
        while ((line = reader.readLine()) != null && !line.isEmpty()){
            String[] header = line.split(":", 2);
            headers.put(header[0].toLowerCase(), header[1].trim());
        }

        // if the method is post manipulate the data in the content
        if ("POST".equalsIgnoreCase(method) && headers.containsKey("content-length")) {
            int contntLen = Integer.parseInt(headers.get("content-length"));

            if (contntLen != 0){
                this.hasContent = true;
                // create a buffer to store the parameter content
                char[] contntChars = new char[contntLen];
                reader.read(contntChars, 0, contntLen);
                String content = new String(contntChars);
                // analyse the code
                String[] params = content.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    parameters.put(key, value);
                }
            }
            else {this.hasContent = false;}
            // if the get method also have data
        } else if ("GET".equalsIgnoreCase(method) && path.contains("?")) {
            String[] pathParam = path.split("\\?");
            this.path = pathParam[0];
            String content = new String(pathParam[1]);
            if (!content.equals("")) {
                this.hasContent = true;
                String[] params = content.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    parameters.put(key, value);
                }
            }else {this.hasContent = false;}
        }
        else{
            System.out.println("Header may have no information");
            System.out.println("Method: "+ method+ " Path: "+ path);
        }
    }
    public String getMethod() {return this.method;} // return request method
    public String getPath() {return this.path;} // return request path
    public HashMap<String, String> getHeaders() {return this.headers;} // return header map
    public HashMap<String,String> getParameters() {return this.parameters;} // return parameter map
    public boolean hasContent() {return this.hasContent;}
    public String getValue(String key) {return this.parameters.get(key);} // return the value associate with key
}
