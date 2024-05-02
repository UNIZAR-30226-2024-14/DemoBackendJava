package org.example;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.ConnectException;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;

public class BlackjackClient extends WebSocketClient {
  // Enum for actions
  public enum Action {
    NONE,
    BET,
    TURN,
    DRAW,
    END,
    INFO
  }

  public static class Pair {
    public Action action;
    public Map<String, String> map;

    public Pair(Action action, Map<String, String> map) {
      this.action = action;
      this.map = map;
    }
  }

  private final Queue<Map<String, String>> messages;
  private final String room;
  private final String access_token;
  public BlackjackClient(String room, String access_token, String base_uri) throws Exception {
    super(new URI(base_uri + room + "?access_token=" + access_token));
    this.messages = new LinkedList<>();
    this.room = room;
    this.access_token = access_token;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    System.out.println("new connection opened");
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    System.out.println("closed with exit code " + i + " additional info: " + s);
  }

  @Override
  public void onError(Exception e) {
    System.err.println("Class: " + e.getClass());
    System.err.println("an error occurred:" + e);
    if (e instanceof ConnectException) {
      System.err.println("The server is probably down.");
    }
  }

  @Override
  public void onMessage(String s) {
    Gson gson = new Gson();
    var map = gson.fromJson(s, Map.class);
    messages.add(map);
    System.out.println("received message: " + s);
  }

  public Pair parseMessage() {
    if (messages.isEmpty()) return new Pair(Action.NONE, null);

    var message = messages.poll();

    if (!message.get("turn").equals(access_token))
      return new Pair(Action.INFO, message);

    // Our turn
    BlackjackClient.Action action = BlackjackClient.Action.NONE;
    if (message.get("state").equals("GameState.BET"))
      action = BlackjackClient.Action.BET;
    else if (message.get("state").equals("GameState.PLAYING"))
      action = BlackjackClient.Action.TURN;
//    else if (message.get("state").equals("GameState.DRAW"))
//      action = BlackjackClient.Action.DRAW;
    else if (message.get("state").equals("GameState.END"))
      action = BlackjackClient.Action.END;

    return new Pair(action, message);

//    if (message.containsKey("action")) {
//      // Only two actions are possible, bet and play
//      if (message.get("action").equals("bet")) {
//        return new Pair(Action.BET, message);
//      } else {
//        return new Pair(Action.TURN, message);
//      }
//    } else if (message.containsKey("actions")) {
//      return new Pair(Action.TURN, message);
//    }
//
//    // If the message does not contain an action, it is info about the game
//    return new Pair(Action.INFO, message);
  }


}
