import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {
	
	// notification
	private String notif = " *** ";
		
	// for input/output
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;                  // to create a socket

	// to create a gui
	private ClientGUI cg;
	
	// the server, the port and the username
	private String server, username;
	private int port;
	
	public String getUsername() {
		return username; // to get the username of the client
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	Client(String server, int port, String username) {
	
		this(server, port, username, null); 
	}

	
	Client(String server, int port, String username, ClientGUI cg) {
		
		// to create a client it takes server, port number 
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
	}
	
	
	 // To start the conversation
	 
	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		// if it fails display an error message
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		// if it's succesfully connected display connection accepted
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		// to create data streams for input and output
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO); // in case of any error, display an exception
			return false;
		}

		// to listen from server
		new ListenFromServer().start();
		// Send the username to the server as a string
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// returns true when it works
		return true;
	}

	
	private void display(String msg) {

		System.out.println(msg); 
		//To send a message to the the GUI screen
	}
	
	// to send a message to the server
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
			System.out.println(msg.toString());
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	//to be able to disconnect when an error occurs
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		
		
		if(cg != null)
			cg.connectionFailed();// to let the user know the connection failed
			
	}
	
	public static void main(String[] args) {
		// default values for the port number 
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		if(!client.start())
			return;
		
		System.out.println("\nHello.! Welcome to the chatroom."); // to let the user know they're entered the chatroom
		
		// to disconnect user when it's done
		client.disconnect();	
	}

	
   class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				// to catch exceptions
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
