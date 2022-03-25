package com.model;

import com.lampsdesign.MessageObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Datasource {

    public static final String DB_NAME = "messaging.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:F:\\Java Projects\\SQLDatabase\\" + DB_NAME;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_NAME = "user_id";
    public static final String COLUMN_TIMESTAMP = "dateTime";
    public static final String COLUMN_MESSAGE = "messageText";

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";


    public static final String QUERY_ALL_MESSAGES = "SELECT " + "*" + " FROM " + TABLE_MESSAGES +
            " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

    public static final String INSERT_MESSAGES = "INSERT INTO " + TABLE_MESSAGES +
            " (" + COLUMN_NAME + ", " +
            COLUMN_TIMESTAMP + ", " +
            COLUMN_MESSAGE +
            " )" +
            "VALUES(?, ?, ?)";

    public static final String QUERY_USER = "SELECT " + "*" + " FROM " + TABLE_USERS +
            " WHERE " + COLUMN_USERNAME + " = ?";

    public static final String QUERY_USER_PASSWORD = "SELECT " + "*" + " FROM " + TABLE_USERS +
            " WHERE " + COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";

    public static final String INSERT_USERS = "INSERT INTO " + TABLE_USERS + "(" + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ") VALUES (?, ?)";


    private Connection conn;

    private static PreparedStatement queryAllMessages;
    private static PreparedStatement insertIntoMessages;
    private static PreparedStatement queryUser;
    private static PreparedStatement queryUserPassword;
    private static PreparedStatement insertIntoUsers;


    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            queryAllMessages = conn.prepareStatement(QUERY_ALL_MESSAGES);
            insertIntoMessages = conn.prepareStatement(INSERT_MESSAGES, Statement.RETURN_GENERATED_KEYS);
            queryUser = conn.prepareStatement(QUERY_USER);
            queryUserPassword = conn.prepareStatement(QUERY_USER_PASSWORD);
            insertIntoUsers = conn.prepareStatement(INSERT_USERS);

            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {

            if (queryAllMessages != null) {
                queryAllMessages.close();
            }

            if (insertIntoMessages != null) {
                insertIntoMessages.close();
            }

            if (queryUser != null) {
                queryUser.close();
            }

            if (queryUserPassword != null) {
                queryUserPassword.close();
            }


            if (insertIntoUsers != null) {
                insertIntoUsers.close();
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    public static List<MessageObject> returnAllMessages() {
        try {
            ResultSet results = queryAllMessages.executeQuery();

            List<MessageObject> messageObjectList = new ArrayList<>();
            while (results.next()) {
                MessageObject messageObject = new MessageObject();
                messageObject.setUserName(results.getString(2));
                messageObject.setDateTime(results.getString(3));
                messageObject.setMessageText(results.getString(4));
                messageObjectList.add(messageObject);
            }

            return messageObjectList;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public static boolean queryUserPassword(String loginCredentials) {
        String[] parts = loginCredentials.split(":");
        String username = parts[0];
        String password = parts[1];

        try {
            queryUserPassword.setString(1, username);
            queryUserPassword.setString(2, password);

            ResultSet resultSet = queryUserPassword.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean queryUser(String username) {
        try {
            queryUser.setString(1, username);

            ResultSet resultSet = queryUser.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean createUser(String loginCredentials) {
        String[] parts = loginCredentials.split(":");
        String username = parts[0];
        String password = parts[1];

        if (!queryUser(username)) {
            try {
                insertIntoUsers.setString(1, username);
                insertIntoUsers.setString(2, password);
                insertIntoUsers.executeUpdate();

                return true;

            } catch (SQLException e) {
                e.getMessage();
                return false;
            }
        }
        return false;
    }

    public static boolean insertMessage(String username, String dateTime, String messageText) throws SQLException {

        insertIntoMessages.setString(1, username);
        insertIntoMessages.setString(2, dateTime);
        insertIntoMessages.setString(3, messageText);

        int affectedRows = insertIntoMessages.executeUpdate();

        if (affectedRows != 1) {
            throw new SQLException("Couldn't insert message!");
        } else { return true; }

    }
}
