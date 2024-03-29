package main.managers.console;

import main.network.Client;
import java.util.Scanner;

public class InputReader extends Thread {
    private final Scanner scan;
    private final Client client;
    private final String HELP = "/help";
    private final String LOGOUT = "/logout";

    public InputReader(Scanner scan, Client client) {
        this.scan = scan;
        this.client = client;
    }

    @Override
    public void run() {
        MessageHelper messageHelper = new MessageHelper();
        while (true) {
            String message = scan.nextLine();
            if (message.contains(HELP)) {
                ConsolePrinter.printHelper();
            } else if (message.contains(LOGOUT)) {
                client.sendMessage(message);
                break;
            } else if (messageHelper.messageCanBeSent(message, client.isClientLoggedIn())) {
                client.sendMessage(message);
            }
        }

        ConsolePrinter.printBye();
        client.stopConnection();
    }
}
