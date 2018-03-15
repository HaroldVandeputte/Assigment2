import java.net.*;
import java.io.*;

public class client{
	/**
	 * Variables
	 * Socket: A socket is an endpoint for communication between two machines.
	 * DataInputStream: A data input stream lets an application read primitive Java data types from an underlying input stream in a machine-independent way.
	 */
	private Socket socket              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;
	private BufferedReader income = null;
	/**
	 * Creating connection to server.
	 * Possible to write HTTP commandos. 
	 * @param 	serverName
	 * 			Name of the server
	 * @param 	serverPort
	 * 			On witch port the server should listen to
	 */
	public client(String serverName, int serverPort){
		
		System.out.println("Establishing connection. Please wait ...");
		System.out.println("Server name:  " + serverName);
		System.out.println("Server port:  " + serverPort);
		System.out.println("----------------------------");
		System.out.println("							");
		try{
			//Open Socket to make a connection
			/**
			 * A socket is one end-point of a two-way communication link between 
			 * two programs running on the network. Socket classes are used to 
			 * represent the connection between a client program and a server 
			 * program. The java.net package provides two classes--Socket and 
			 * ServerSocket--that implement the client side of the connection and 
			 * the server side of the connection, respectively.
			 */
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			console   = new DataInputStream(System.in);
		    streamOut = new DataOutputStream(socket.getOutputStream());
		    income = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		}
		catch(UnknownHostException uhe){
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		
		// Initialize line
		String out = "";
		StringBuilder lineOut = new StringBuilder();
		String lineIn = null;
		StringBuilder response = new StringBuilder();
		
		// While function so that the client can keep on giving commands.
		// When the client types '.bye' the connection will be closed.
	    while (!out.equals(".bye")){  
	    	try{
	    		
	    		out = console.readLine();
	    		lineOut.append(out);
	    		lineOut.append(System.getProperty("line.separator"));

	    		while(!out.equals("")){
	    			out = console.readLine();
		    		lineOut.append(out);
		    		if(!out.equals(""))
		    			lineOut.append(System.getProperty("line.separator"));
	    		}
	    		
	    		streamOut.writeUTF(lineOut.toString());
	    		streamOut.flush();
	    		System.out.println(lineOut.toString());
	            while ((lineIn = income.readLine()) != null) {
	            	response.append(lineIn);
	            	response.append(System.getProperty("line.separator"));
	            }
	            System.out.println("RESPONSE: ");
	            System.out.println(response.toString());
	    		
	         }
	         catch(IOException ioe){
	        	 System.out.println("Sending error: " + ioe.getMessage());
	         }
	      }
      }
	
	/**
	 * Close the connection
	 */
	public void stop(){  
		try{  
			if (console   != null)  console.close();
	        if (streamOut != null)  streamOut.close();
	        if (socket    != null)  socket.close();
	        if (income 		  != null)  income.close();
	    }
	    catch(IOException ioe){ 
	    	System.out.println("Error closing ...");
	    }
	}
	
	/**
	 * MAIN
	 */
	public static void main(String args[]){  
		client client = null;
	    if (args.length != 2)
	    	System.out.println("Usage: java ChatClient host port");
	    else
	    	client = new client(args[0], Integer.parseInt(args[1]));
	}

 }