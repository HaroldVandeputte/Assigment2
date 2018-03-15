import java.net.*;
import java.io.*;

/**
 * 
 * @author Jonas Wittouck
 * @author Harold Vandeputte
 *
 */
public class ChatClient {
	/**
	 * Initialize variables for the connection
	 * 
	 * clientSocket: a connection is made through the socket
	 * in: gets the response of the server
	 * output: sends data through the socket to the server
	 * writer: writes data to the file file.html so it is saved
	 */
	Socket clientSocket = null;
	BufferedReader in = null;
	PrintWriter output = null;
	//PrintWriter writer = new PrintWriter("file.html", "UTF-8");

	private ChatClient(String HTTPcommand, String URI, int port) throws Exception{
		/**
		 * Initialize variables
		 * 
		 * hostServer = servers identity
		 * path = path of the URI
		 * portNumber = port to with the server/client should listen to
		 * HTTPMethod = HTTP method 
		 * method = first line of the HTTP request 
		 * host = second line of the HTTP request
		 * in = BufferReader for this socket
		 */
		String hostServer = "";
		String path = "";
		int portNumber = port;
		String HTTPMethod = HTTPcommand;
		String methodLine = "";
		String hostLine= "";
		
		
		// Split the URI up into two parts: the host server and the path
		String[] splitURI = splitURI(URI);
		hostServer = splitURI[0];
		path = splitURI[1];
		
		try {	
// 1 - Open socket to make a connection.
		    clientSocket = new Socket(hostServer, portNumber);
		    
// 2 - Open an input stream and output stream to the socket.
		    output = new PrintWriter(clientSocket.getOutputStream(), true);
		    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch(UnknownHostException uhe){
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		
		// See what method is given
		// For each method the method-line and host-line will be different
		switch (HTTPMethod) {
			case("GET"):
				methodLine =  "GET " + path + " HTTP/1.1";
				hostLine = "Host:" + hostServer + ":" + portNumber;
				break;
			case("HEAD"):
				//TODO
			case("PUT"):
				//TODO
			case("POST"):
				//TODO
			default: throw new IllegalArgumentException("Invalid HTTP method (for this assignment)" + HTTPMethod);
		}
		
		
		
		
// 3 - Read from and write to the stream according to the server's protocol.
		
		// Form the request
		String request = methodLine + '\n' + hostServer + '\r' + '\n';
		// Send to server
		output.println(request);
		
		/**
		 * Retrieve the input line per line and build the response.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		String inputLine = in.readLine();
		System.out.println(inputLine);
		//writer.println(inputLine);
		//boolean canBeWritten = false;
		while(in.ready()){
			inputLine = in.readLine();
			System.out.println(inputLine);
			//if(inputLine.contains("html>"))
			//	canBeWritten = true;
			//if(canBeWritten)
			//	writer.println(inputLine);
		}
        
 // 4 - Close the streams and socket
		stop();
	}
	
	/**
	 * Split up an URI in the server host and the path
	 * @param URI
	 * @return result == [hostServer, path]
	 */
	public String[] splitURI(String URI){
		String[] result = {"",""};
		result = URI.split("(?=/)",2);
		return result;
	}
	
	/**
	 * Close the connection
	 */
	public void stop(){  
		try{  
	        if (output 			!= null)  output.close();
	        if (in 		  		!= null)  in.close();
	        if (clientSocket    != null)  clientSocket.close();
	        //if (writer			!= null) writer.close();
	    }
	    catch(IOException ioe){ 
	    	System.out.println("Error closing ...");
	    }
	}
	
	/**
	 * MAIN
	 * @throws Exception 
	 */
	public static void main(String args[]) throws Exception{  
		ChatClient chatClient = null;
	    try{
	    	chatClient = new ChatClient(args[0],args[1], Integer.parseInt(args[2]));
	    }
	   	//If the port number is not given then the default port is 80
	    catch(IndexOutOfBoundsException exc)
	    {
	    	chatClient = new ChatClient(args[0],args[1], 80);
	    }
	}
}