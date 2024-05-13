package org.example;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;

public class Main {
  private static final String ws_uri = "ws://localhost:8000";
  private static final String blackjack_uri = "/ws/blackjack/";
  private static final String chat_uri = "/ws/chat/";
  public static void main(String[] args) throws Exception {
    String room = "1";
    String access_token = "";
    for (int i = 0; i < 5; i++) {
      access_token += (char)('a' + (int)(Math.random() * 26));
    }
//    access_token = "unizar";
    System.out.println("Access token: " + access_token);

//    String what = "chat";
    String what = "blackjack";

    WebSocketClient client;
    if (what.equals("blackjack"))
      client = new BlackjackClient(room, access_token, ws_uri + blackjack_uri);
    else
      client = new ChatClient(room, access_token, ws_uri + chat_uri);

    client.connect();

    System.out.println("Connecting to websocket server...");
    while (!client.isOpen() && !client.isClosed()) {
      Thread.sleep(100);
    }

    if (client.isClosed()) {
      System.out.println("Connection failed.");
      System.exit(1);
    }

    Scanner scanner = new Scanner(System.in);

    if (what.equals("chat")) {
      System.out.println("Connected to chat server.");
      System.out.print("Enter your message: ");
      while (client.isOpen()) {
        String message = scanner.nextLine();
        if (message.equals("exit")) {
          break;
        }
        String msg = "{\"message\": \"" + message + "\"}";
        client.send(msg);
      }
    } else {
      while (client.isOpen()) {
        BlackjackClient.Pair pair = ((BlackjackClient)client).parseMessage();
        BlackjackClient.Action action = pair.action;
        var message = pair.map;

        // Pretty print
        if (message != null && !message.isEmpty()) {
          System.out.println("Game state:");
          for (Map.Entry<?, ?> entry : message.entrySet()) {
            System.out.println("\t"+ entry.getKey() + ": " + entry.getValue());
          }
        }

        switch (action) {
          case BET:
            System.out.print("Enter bet amount: ");
            String bet = scanner.nextLine();
            if (bet.equals("pause")) {
              client.send("{\"action\": \"pause\"}");
              break;
            }
            client.send("{\"action\": \"bet\", \"value\": \""+ bet + "\"}");
            break;
          case TURN:
//            System.out.println(message);
            System.out.print("Enter action (hit/stand): ");
            String turn = scanner.nextLine();
            if (turn.equals("pause")) {
              client.send("{\"action\": \"pause\"}");
              break;
            }
            client.send("{\"action\": \""+ turn + "\"}");
            break;
          case DRAW:
            System.out.println("Drawing card...");
            System.out.println(message);
            break;
          case END:
            System.out.println("Game ended.");
            System.out.println(message);
            break;
          case INFO:
            System.out.println(message);
            break;
          case NONE:
          default: break;
        }
      }
    }

    scanner.close();
    client.close();
  }
}