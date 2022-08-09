package main.network;

import main.managers.JsonMapperImpl;
import main.managers.SubtitlesPrinter;
import main.messageTypes.*;
import main.scenes.LoginStatusListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class TcpClient implements Client {
    //CONNECTION
    private final String ip;
    private final int port;
    private Socket clientSocket;
    private boolean isClientLoggedIn;
    private boolean clientConnected = false;
    //RECEIVING AND SENDING
    private PrintWriter serverPrintWriter;
    private BufferedReader serverReceivedJSON;
    //LISTENERS
    private MessageListener messageListener;
    private LoginStatusListener loginStatusListener;
    //OTHERS
    private ArrayList<String> onlineUsers;
    private final JsonMapperImpl jsonMapper = new JsonMapperImpl();

    public TcpClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        connect(ip, port);
        while (true) {
            if (!clientSocket.isClosed()) {
                MessageType messageType = receiveMessage();

                if (messageType instanceof Login) {
                    isClientLoggedIn = ((Login) messageType).isLoginSuccessful();
                    System.out.println("Received login status");
                    loginStatusListener.onLoginStatusReceived();
                } else if (messageType instanceof Message message) {
                    messageListener.onMessageReceived(new Message(message.getSender(), message.getMessage()));
                } else if (messageType instanceof UsersListReceiver) {
                    onlineUsers = ((UsersListReceiver) messageType).getUsers();
                    messageListener.onUsersListReceived();
                } else if (messageType instanceof Logout) {
                    SubtitlesPrinter.printReceivedMessage((((Logout) messageType).getMessage()));
                    break;
                } else if (messageType instanceof Register) {
                    System.out.println("Received register status");
                    isClientLoggedIn = ((Register) messageType).isLoginSuccessful();
                    loginStatusListener.onLoginStatusReceived();
                }
            } else {
                break;
            }
        }
    }

    private void connect(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            SubtitlesPrinter.printConnectionEstablished();
            clientConnected = true;
        } catch (IOException e) {
            SubtitlesPrinter.printConnectionProblems();
            SubtitlesPrinter.printReconnectingUnsuccessful();
            try {
                sleep(5000);
                connect(ip, port);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public MessageType receiveMessage() {
        try {
            //Waiting for server input
            serverReceivedJSON = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return jsonMapper.mapJson(serverReceivedJSON.readLine());
        } catch (IOException e) {
            SubtitlesPrinter.printLostConnection();
            isClientLoggedIn = false;
            clientConnected = false;
            connect(ip, port);
        }
        return null;
    }

    @Override
    public void sendMessage(String msg) {
        try {
            serverPrintWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            serverPrintWriter.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopConnection() {
        try {
            serverReceivedJSON.close();
            serverPrintWriter.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isClientLoggedIn() {
        return isClientLoggedIn;
    }

    @Override
    public boolean isClientConnected() {
        return clientConnected;
    }

    public ArrayList<String> getOnlineUsers() {
        return onlineUsers;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setLoginStatusListener(LoginStatusListener loginStatusListener) {
        this.loginStatusListener = loginStatusListener;
    }
}
