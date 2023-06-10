package com.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Authenticator {
    private final HashMap<String, String> userBook; // {user : pwd}
    private final String path = "./User.txt";

    // read the user file to prepare the login match map
    public Authenticator() throws IOException {
        userBook = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValue = line.split("&");
                String username = keyValue[0].split("=")[1];
                String password = keyValue[1].split("=")[1];
                userBook.put(username, password);
            }
        }
    }

    // perform login authentication
    public boolean authenticate(String username, String password) {
        return userBook.containsKey(username) && userBook.get(username).equals(password);
    }
}
