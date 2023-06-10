package com.server;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import com.server.Session;
import com.server.UserConnectHandler;

// The server will use port 8080 as the user response port
// and 8081 for python QB and 8082 for Java QB
public class Server {
    private HashMap<String,Session> sessionHandler; // manage a session for loged in user.
    private Authenticator auth; // authenticate the user
    private ServerSocket tmServer; // ServerSocket object
    private ServerSocket pythonQB; // Server for python QB connection
    private ServerSocket javaQB; // Server for java QB connection
    private boolean isRunning;
    public void deploy(int port) throws IOException{
        try {
            sessionHandler = new HashMap<>();
            auth = new Authenticator(); // create user checker

            pythonQB = new ServerSocket(8081);
            System.out.println("Created a server for pythonqb.");
            Socket pyQb = pythonQB.accept();
            System.out.println("pyQb connected");
            QbRequester pyQB = new QbRequester(pyQb);
            javaQB = new ServerSocket(8082);
            System.out.println("Created a server for javaqb.");
            Socket javaQb = javaQB.accept();
            System.out.println("javaQb connected");
            QbRequester jaQB = new QbRequester(javaQb);
            tmServer = new ServerSocket(port);
            System.out.println("Created a server.");
            isRunning = true;
            receive(pyQB, jaQB);
        }
        catch (IOException e) {
            System.err.println("Server deploy failed! " + e);
            stop();
        }
    }

    public void receive(QbRequester pyQB, QbRequester javaQB){
        // continuing run the server to service for the student
        while (isRunning){
            try{
                Socket client = tmServer.accept();
                System.out.println("Received a new connection");
                new UserConnectHandler(client, sessionHandler, auth, pyQB, javaQB).process();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Client error occur.");
            }
        }
    }

    public void stop(){
        isRunning = false;
        try{
            this.tmServer.close();
            System.out.println("Server closed.");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
            Server server = new Server();
            server.deploy(8080);
    }
}