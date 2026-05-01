package com.example;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;



public class SocketClientExample {
	
	
	/*
	 * Modify this example so that it opens a dialogue window using java swing, 
	 * takes in a user message and sends it
	 * to the server. The server should output the message back to all connected clients
	 * (you should see your own message pop up in your client as well when you send it!).
	 *  We will build on this project in the future to make a full fledged server based game,
	 *  so make sure you can read your code later! Use good programming practices.
	 *  ****HINT**** you may wish to have a thread be in charge of sending information 
	 *  and another thread in charge of receiving information.
	*/
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        

        // Initialization of the stuff

       InetAddress host = InetAddress.getLocalHost();
        Socket socket = new Socket(host.getHostName(), 9876);

        // 1. Initialize OUTPUT first and FLUSH. 
        // This sends the header so the server's OIS can unblock.
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush(); 

        // 2. Now initialize INPUT. It shouldn't hang now.
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        
        System.out.println("we are connected");

        // 3. Reader Thread needs a loop to keep listening
        Thread readerThread = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    Object obj = ois.readObject();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        System.out.println("[Server]: " + message);
                    }
                }
            } catch (Exception e) {
                System.out.println("Reading stopped: " + e.getMessage());
            }
        });
         // Allows JVM to exit even if this thread is running
        readerThread.start();

        // Now, this is for inputs:

        Scanner newScanner = new Scanner(System.in);
        System.out.println("Connected. Type messages below:");


        while (true) {
            String input = newScanner.nextLine();
            oos.writeObject(input);
            oos.flush();

            if (input.equalsIgnoreCase("exit")) break;
        }
       
        newScanner.close();
        socket.close();
    
    }
}
