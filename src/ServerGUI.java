import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	private JButton stopStart;
	private JTextArea chat, event;
	private JTextField tPortNumber;
	private Server server;
	
	
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		JPanel north = new JPanel(); // to create a panel for the chat server
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		// a new button called Start to start the server
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH); // to add the buttons to the panel
		
		// a panel for chat room
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));	
		add(center);
		
		// WindowListener for when the user wants to exit the page by clicking the button
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		

	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		// to stop the server
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}
      	// to start the servet
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number"); // when a wrong port number is given
			return;
		}
		// to create a new Server
		server = new Server(port, this);
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
	}
	
	public static void main(String[] arg) {
		// start the server with the port number 1500 by default
		new ServerGUI(1500);
	}

	public void windowClosing(WindowEvent e) {
		if(server != null) {
			try {
				// to close the connection
				server.stop();			
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		// exit the window frame
		dispose();
		System.exit(0);
	}
	//ignore the window events
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	

	// to run the server as thread
	class ServerRunning extends Thread {
		public void run() {
			server.start();        
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n"); // when the server is not started, display that the server is crashed
			server = null;
		}
	}

}