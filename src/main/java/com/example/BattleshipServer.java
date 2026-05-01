package com.example;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BattleshipServer {

    private static List<ConnectionHandler> clients = new CopyOnWriteArrayList<>();
    private static Battleship game = new Battleship(10);

    public static final int LISTENING_PORT = 9876;

    public static void main(String[] args) {

        ServerSocket listener;
        Socket connection;

        try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("Battleship Server listening on port " + LISTENING_PORT);
            while (true) {
                UUID uuid = UUID.randomUUID();
                String uuidAsString = uuid.toString();

                connection = listener.accept();
                System.out.println("Connection accepted!");

                ConnectionHandler handler = new ConnectionHandler(connection, uuidAsString);
                clients.add(handler);
                handler.start();  
            }
        } catch (Exception e) {
            System.out.println("Server shut down. Error: " + e);
        }
    }

    private static class ConnectionHandler extends Thread {

        Socket client;
        public String uuid;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private int playerNumber;

        ConnectionHandler(Socket socket, String id) {
            client = socket;
            uuid = id;
            // First to connect is Player 1, second is Player 2
            this.playerNumber = clients.size() + 1;
        }

        public void handleDisconnect() {
            try {
                client.close();
                clients.remove(this);
                System.out.println("Player [" + uuid + "] disconnected.");
            } catch (Exception e) {
                System.out.println("Error during disconnect.");
            }
        }

        public void sendMessage(String message) {
            try {
                oos.writeObject(message);
                oos.flush();
            } catch (Exception e) {
                System.out.println("Error sending message to player " + playerNumber);
            }
        }

        private static void broadcastGameState() {
            for (ConnectionHandler handler : clients) {
 
                String state = game.getGameState(handler.playerNumber == 1);
                handler.sendMessage(state);
            }
        }

        @Override
        public void run() {
            String clientAddress = client.getInetAddress().toString();
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();

                ois = new ObjectInputStream(client.getInputStream());

                System.out.println("New player connected: " + clientAddress + " as Player " + playerNumber);
                
      
                sendMessage(game.getGameState(playerNumber == 1));

                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof String) {
                        String command = (String) obj;
                        System.out.println("Command received from Player " + playerNumber + ": " + command);
                        
                        if (command.startsWith("SHOOT")) {
                            String[] parts = command.split(" ");
                            if (parts.length == 3) {
                                int row = Integer.parseInt(parts[1]);
                                int col = Integer.parseInt(parts[2]);
                                
                      
                                game.shoot_grid(row, col, playerNumber == 1);
                                
                          
                                broadcastGameState();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                handleDisconnect();
                System.out.println("Error on connection with: " + clientAddress + ": " + e);
            }
        }
    }
}
