package com.lampsdesign;

import com.model.Datasource;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private boolean loggedOn;

    public ClientHandler(Socket socket) {
        loggedOn = false;

        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!loggedOn && socket.isConnected()) {
                String loginCredentials = bufferedReader.readLine();

                if (loginCredentials.equals("XXXXX DISCONNECT XXXXX")) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    continue;
                }

                clientUsername = loginCredentials.split(":")[0];

                if (Objects.equals(loginCredentials.split(":")[2], "0")) {
                    //Standard logon
                    loggedOn = Datasource.queryUserPassword(loginCredentials);

                    if (loggedOn) {
                        System.out.println(clientUsername + " has logged on");
                        bufferedWriter.write("OK");
                    } else {
                        System.out.println(clientUsername + " Invalid username or password");
                        bufferedWriter.write("Denied");
                    }
                } else {
                    //User sign up attempt
                    if (!Datasource.queryUser(clientUsername)) {
                        if (Datasource.createUser(loginCredentials)) {
                            System.out.println(clientUsername + " has been created");
                            bufferedWriter.write("OK");
                        } else {
                            System.out.println(clientUsername + " SQL issue sign up failed");
                            bufferedWriter.write("Denied");
                        }

                    } else {
                        System.out.println(clientUsername + " already exists, sign up failed");
                        bufferedWriter.write("Denied");
                    }
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            if (socket.isConnected()) {
                clientHandlers.add(this);

                recoverMessages();
                broadcastMessage("SERVER", clientUsername + " has entered the chat");
            }

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void recoverMessages() {
        List<MessageObject> allMessages = Datasource.returnAllMessages();

        for (MessageObject message: allMessages) {
            try {
                bufferedWriter.write(message.getUserName() + ": " + message.getMessageText());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected() && !socket.isClosed()) {
            try {
                messageFromClient = bufferedReader.readLine();

                if (messageFromClient.equals("XXXXX DISCONNECT XXXXX")) {
                    break;
                }

                broadcastMessage(this.clientUsername, messageFromClient);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                System.out.println(this.clientUsername + " has disconnected");
                break;
            }
        }
        closeEverything(socket, bufferedReader, bufferedWriter);
    }

    public void broadcastMessage(String clientUsername, String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    if (clientUsername == null) {
                        clientHandler.bufferedWriter.write(messageToSend);
                    } else {
                        clientHandler.bufferedWriter.write(clientUsername + ": " + messageToSend);
                    }
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try {
            Datasource.insertMessage(clientUsername, dtf.format(now), messageToSend);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);

        if (loggedOn) {
            broadcastMessage("SERVER", clientUsername + " has left the chat!");
        }
    }


    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        removeClientHandler();

        try {
            if (socket != null) {
                socket.close();
            }

            if (bufferedReader != null ) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
