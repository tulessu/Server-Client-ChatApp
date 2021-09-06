
import java.io.*;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
	private static int uniqueId;
	private ArrayList<ClientThread> al;
	private ServerGUI sg;
	private SimpleDateFormat sdf;
	private int port;
	private boolean keepGoing;
	private String notif = " *** ";
	

	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		this.sg = sg;
		// the port number
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList to keep the Client list
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		// to create socket server and wait for connection requests //
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// as long as the keepGoing is true, keep waiting for clients
			while(keepGoing) 
			{
				// to let the user know that the server is waiting for clients
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	// accept connection
				// to stop the connection
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // to create clients, thread is used
				al.add(t);									// keep it in the ArrayList
				t.start();
			}
			
			try {
				serverSocket.close(); // to close the server
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						
					}
				}
			}
			catch(Exception e) {
				// if any exception occurs, display an exception message
				display("Exception closing the server and clients: " + e); 
			}
		}
		
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
		
	}		
    // to stop the server
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	
	// to create a method called broadcast to send messages to everyone
	private synchronized boolean broadcast(String message) {
		// to add the time to the messages
		String time = sdf.format(new Date());
		String[] w = message.split(" ",3);
		
		// to check if the message is private (1-1) 
		boolean isPrivate = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		
		// if it's private message, send the message only to the mentioned user by using @
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			
			message=w[0]+w[2];
			String messageLf = time + " " + message + "\n";
			System.out.println(messageLf + "\n" + "to the " + tocheck);
			boolean found=false;
			// to find the mentioned user name
			for(int y=al.size(); --y>=0;)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					// 
					if(!ct1.writeMsg(messageLf)) {
						al.remove(y);
						display("Disconnected Client " + ct1.username + " removed from list.");
					}
					// if the message is delivered to the correct person, make it true
					found=true;
					
					break;
				}}
			// if the mentioned user is not found, return false
			if(found!=true)
			{
				return false; 
			}
		}
		// if message is a broadcast message to all of the users
		else
		{
			String messageLf = time + " " + message + "\n";
			// display message
			System.out.print(messageLf);
			
			for(int i = al.size(); --i >= 0;) {
				ClientThread ct = al.get(i);
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
		return true;
	}

	// if client clicks to LOGOUT button
		synchronized void remove(int id) {
			
			String disconnectedClient = "";
			for(int i = 0; i < al.size(); ++i) {
				ClientThread ct = al.get(i);
				// when the user who logs out is found, remove the user from the list
				if(ct.id == id) {
					disconnectedClient = ct.getUsername();
					al.remove(i);
					break;
				}
			} 
			// display a message to let the other users know when someone left the chatroom
			broadcast(notif + disconnectedClient + " has left the chat room." + notif);
		}
	
	
	
	public static void main(String[] args) {
		// by default start the port number as 1500
		int portNumber = 1500;
		
		// to create and start the server
		Server server = new Server(portNumber);
		server.start();
	}

	// thread is used for each client 
	class ClientThread extends Thread {
		
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// every client has a unique id
		int id;
		// the Username of the user (client)
		String username;
		ChatMessage cm;
		String date; // the date for when the client is connected

		ClientThread(Socket socket) {
			id = ++uniqueId;
			this.socket = socket;
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected."); // to let the other users know when someone connected to the chatroom
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public void run() {
			boolean keepGoing = true;
			while(keepGoing) { // as long as the keepGoing is true, which means until the user wants to log out
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = cm.getMessage();

				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation=broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "Sorry. No such user exists." + notif; // if there is no user as mentioned, display this
						writeMsg(msg);
					}
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message."); // when a user disconnects display this
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					// if we want to see who is active at the moment
					String whoisstring = "List of the users connected at " + sdf.format(new Date()) + "\n"; 
					writeMsg(whoisstring);
					System.out.println(whoisstring);
		
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			remove(id);
			close();
		}
		
		// to close the application
		private void close() {
			// to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, let the user know that the message is not delivered
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}