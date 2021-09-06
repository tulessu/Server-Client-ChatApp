
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;


import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel label , usermail, usermailadress, label2,label3, msg_label;
	private JTextField tf,tf2,tf3;
	private JPasswordField password;
	private JTextField tfServer, tfPort;
	private JButton login, logout, whoIsIn, signup, admin;
	private JTextArea ta;
	private boolean connected;
	private Client client;
	private int defaultPort;
	private String defaultHost;

	// Constructor to get a socket number
	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		JPanel northPanel = new JPanel(new GridLayout(12,1));
		// the server name and the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		// adds the Server an port field to the GUI
		northPanel.add(serverAndPort);

		// the Label and the TextField
		setUsermail(new JLabel("User e-mail:"));
		usermailadress= new JLabel("");
		//to ask the user for their informations these labels are used
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		label2 = new JLabel("Enter your e-mail address below", SwingConstants.CENTER);
		label3 = new JLabel("Enter your password below", SwingConstants.CENTER);
		msg_label = new JLabel("Enter your message below", SwingConstants.CENTER);
		
		// to add all the labels and empty text fields to the panel
		northPanel.add(getUsermail());
		northPanel.add(usermailadress);
		northPanel.add(label);
		tf = new JTextField("");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		
		northPanel.add(label2);
		tf2 = new JTextField("");
		tf2.setBackground(Color.WHITE);
		northPanel.add(tf2);
		
		northPanel.add(label3);
		password = new JPasswordField("");
		password.setBackground(Color.WHITE);
		northPanel.add(password);
		
		northPanel.add(msg_label);
		msg_label.setVisible(false);
		tf3 = new JTextField("");
		tf3.setBackground(Color.WHITE);
		northPanel.add(tf3);
		tf3.setVisible(false);
		
		add(northPanel, BorderLayout.NORTH);

		// Panel for the chat room
		ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// create the buttons for the GUI
		signup = new JButton("SignUp");
		signup.addActionListener(this);
		admin = new JButton("Admin");
		admin.addActionListener(new Action1());
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);	
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);

		JPanel southPanel = new JPanel();
		southPanel.add(admin);
		southPanel.add(signup);
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// to append the texts in the TextArea
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	// to reset our buttons, label, textfield if the connection fails
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your e-mail address below");
		tf.setText("");
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
		}
	  JTextField email = new JTextField("");
	  JPasswordField password_sgn = new JPasswordField();
	  
	  private void signUp(JFrame frame) {
	        JPanel p = new JPanel(new BorderLayout(5,5));

	        JPanel labels = new JPanel(new GridLayout(0,1,2,2));
	        labels.add(new JLabel("E-mail", SwingConstants.TRAILING));
	        labels.add(new JLabel("Password", SwingConstants.TRAILING));
	        p.add(labels, BorderLayout.LINE_START);

	        JPanel controls = new JPanel(new GridLayout(0,1,2,2));
	        
	        controls.add(email);
	       
	        controls.add(password_sgn);
	        p.add(controls, BorderLayout.CENTER);

	        JOptionPane.showMessageDialog(frame, p, "SignUp", JOptionPane.QUESTION_MESSAGE);
	        System.out.println("E-mail: " + email.getText());
	        System.out.println("Password: " + new String(password_sgn.getPassword()));
	

	    }
		
	//Events for when the user clicks buttons
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, "")); // displays a message for the other users
			usermailadress.setText("");
			tf.setVisible(true);
			tf2.setVisible(true);
			password.setVisible(true);
			label.setVisible(true);
			label2.setVisible(true);
			label3.setVisible(true);
			
			msg_label.setVisible(false);
			tf3.setVisible(false);
			ta.setText("Welcome to the Chat room");
			
			return;
		}
		// if it the who is in button
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			
			return;
			
		}
		if(connected) {
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf3.getText()));				
			tf3.setText("");
			return;
		}
		
		
		if(o == signup) {
			signUp(null);
			
			if (email.getText().equals("")  || password_sgn.getText().equals("")) {// if the email or the password is invalid, display this
				System.out.println("You have to enter a valid email and password!");
				JOptionPane.showMessageDialog(new JFrame(), "You have to enter email and password!", "ERROR",
				        JOptionPane.ERROR_MESSAGE);
				signUp(null);
			}
			else {
		        JFrame frame = null;
		        //if the user entered their mail address and password they are successfully registered
				int result = JOptionPane.showConfirmDialog(frame, "You are successfully registered!",
	                    "Confirmation Message", JOptionPane.OK_CANCEL_OPTION);
		        
		        
	            // yaðýz burasý ok butonunu kontrol ettiðimiz yer!!
				if (result  == JOptionPane.OK_OPTION) {
				// OK button was pressed
					System.out.println("-signup done-");
				} else if (result  == JOptionPane.CANCEL_OPTION) {
				// Cancel button was pressed
					System.out.println("signup not done");
				}
			}
			  
		}
		

		if(o == login) {
			
			// connection request
			String username = tf.getText().trim();
			usermailadress.setText(username);
			if(username.length() == 0) // means empty username
				return;
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			//invalid port number
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;  
			}

			// to create a new Client
			client = new Client(server, port, username, this);
			// test if we can start the Client
			if(!client.start()) 
				return;
			
			msg_label.setVisible(true);
			tf3.setVisible(true);
			
			tf.setText("");
			tf2.setText("");
			password.setText("");
			
			tf.setVisible(false);
			tf2.setVisible(false);
			password.setVisible(false);
			label.setVisible(false);
			label2.setVisible(false);
			label3.setVisible(false);
			
			
			connected = true;
			
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf3.addActionListener(this);
		}

	}
	// for the admin button
    static class Action1 implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	
        	Object o = e.getSource();
        	
            JFrame Frame1 = new JFrame("Superuser Log-In Page");
            JPanel panel1 = new JPanel(new GridLayout(5,1));

            
            JLabel label1 = new JLabel("Enter your E-mail adress below:");
            JTextField txt1 = new JTextField();
            JLabel label2 = new JLabel("Enter your password below:");
            JPasswordField password2 = new JPasswordField();
            JButton btn_signup = new JButton("Log in");
            btn_signup.addActionListener(new Action2());
            Frame1.setVisible(true);
            Frame1.setSize(400,300);
            Frame1.setDefaultCloseOperation(EXIT_ON_CLOSE);
            panel1.add(label1);
            panel1.add(txt1);
            panel1.add(label2);
            panel1.add(password2);
            panel1.add(btn_signup);
            Frame1.add(panel1);
            
         
        }
    
    }
    
    static class Action2 implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	
        	Object o = e.getSource();
        	
            JFrame Frame1 = new JFrame("Superuser Log-In Page");
            JPanel panel1 = new JPanel(new BorderLayout());
  
            
            JTextArea txt_area = new JTextArea("",600,100);
            txt_area.setEditable(false);
            
            // to write all the messages to a file, which only the superuser (admin) can access.
            BufferedReader br1;
			try {
				br1 = new BufferedReader(new FileReader("C:\\Users\\ertug\\eclipse-workspace\\chatappfinal\\src\\output2.txt"));
				StringBuilder sb = new StringBuilder();
                String line = br1.readLine();
                while (line != null) {
                    sb.append(line+"\n");
                   line = br1.readLine();
                   }
                String everything = sb.toString();
                txt_area.setText(everything);
			} catch (IOException e2) {
				
				e2.printStackTrace();
			}
   
			Frame1.setVisible(true);
            Frame1.setSize(600,600);
            Frame1.setDefaultCloseOperation(EXIT_ON_CLOSE);
            panel1.add(new JScrollPane(txt_area));
            
            Frame1.add(panel1);
            
         
        }
    
    }
    
	// to start the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

	public JLabel getUsermail() {
		return usermail;
	}

	public void setUsermail(JLabel usermail) {
		this.usermail = usermail;
	}

}