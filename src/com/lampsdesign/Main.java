package com.lampsdesign;

import com.model.Datasource;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) {

        Datasource datasource = new Datasource();

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!datasource.open()) {
            System.out.println("Can't open datasource");
            return;
        }


        Server server = new Server(serverSocket);
        server.startServer();

        server.closeServer();
        datasource.close();
    }
}
