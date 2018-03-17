import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;

/**
 * 
 * @author Jonas Wittouck
 * @author Harold Vandeputte
 *
 */
public class ChatClient {


	/*********************************************************************************
	 * 			VARIABLES
	 ********************************************************************************/

	/**
	 * Initialize variables for the connection
	 * 
	 * clientSocket: a connection is made through the socket
	 * in: reads the response of the server
	 * output: sends data through the socket to the server
	 * writer: writes data to the file file.html so it is saved
	 */
	Socket clientSocket = null;
	InputStream inStream = null;
	//BufferedReader inBuf = null;
	PrintWriter output = null;
	FileOutputStream fos = null;

	//PrintWriter writer = new PrintWriter("file.html", "UTF-8");


	/*********************************************************************************
	 *			CLASS
	 ********************************************************************************/

	private ChatClient(String HTTPcommand, URI uri, int port) throws Exception{

		/**
		 * Initialize variables
		 * 
		 * hostServer = servers identity
		 * path = path of the URI
		 * portNumber = port to with the server/client should listen to
		 * HTTPMethod = HTTP method 
		 * in = BufferReader for this socket
		 */
		String hostServer = uri.getHost();
		String path = uri.getPath();
		int portNumber = port;
		String HTTPMethod = HTTPcommand;
		List<String> allImages = new ArrayList<String>();


		/*********************************************************************************
		 * 	CONNECTION OPEN
		 ********************************************************************************/

		try {	
			// 1 - Open socket to make a connection.
			clientSocket = new Socket(hostServer, portNumber);

			// 2 - Open an input stream and output stream to the socket.
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			inStream = clientSocket.getInputStream();
		}catch(UnknownHostException uhe){
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}

		// Form the request dependend on with method is given
		String request = "";
		switch (HTTPMethod) {
		case("GET"):
			request = getHTTP1Request("GET", path, hostServer, portNumber, false);
		break;
		case("HEAD"):
			request = getHTTP1Request("HEAD", path, hostServer, portNumber, false);
		break;
		case("PUT"):
			//TODO
		case("POST"):
			//TODO
		default: throw new IllegalArgumentException("Invalid HTTP method (for this assignment)" + HTTPMethod);
		}


		/*********************************************************************************
		 * 	READ AND WRITE
		 ********************************************************************************/		

		/* 3 - Read from and write to the stream according to the server's protocol. */

		/* Send to server */
		output.println(request);
		/**
		 * Retrieve the input line per line and build the response.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		byte[] b = new byte[1024]; // 1 kB read blocks.
		/* Depending on the http-method the amount of ending bytes will be different */
		/* GET - 2 times (head and body); HEAD - 1 time (head) */
		int  count = 0;
		fos = new FileOutputStream("file.html"); //file to store the data
		int i = 0;
		int test = 0;

		while((i = inStream.read(b)) != -1){
			/* Reads 1024 numbers of bytes from the input stream and stores them 
			 * into the buffer array b.
			 */
			String str = new String(b, StandardCharsets.UTF_8);// transform to string
			System.out.println(str); 

			// Looking for images
			if(str.contains("img")){
				String src = getSrc(str);
				allImages.add(src);
			}

			fos.write(b, 0, i);

			if(b[i-1] ==  10){
				/*System.out.println(b[i-4]);
	    		System.out.println(b[i-3]);
	    		System.out.println(b[i-2]);
	    		System.out.println(b[i-1]);*/
				count ++; // '10' shows the end of the HEAD of the BODY
			}
			//if(count == 2 && HTTPMethod.equals("GET") ) break;
			if(count == 1 && HTTPMethod.equals("HEAD") ) break;
		}
		if (fos 				!= null)  fos.close(); // Close the stream for later uses
		/**
	    /* Download all images 
	    String pathImage = changePathForImage(path);
	    for(int k = 0; k < allImages.size(); k++){
	    	boolean headerFound = false;
	    	String newPath = pathImage + allImages.get(k);
	    	String newRequest = getHTTP1Request("GET",newPath,hostServer,portNumber, true);
	    	output.println(newRequest);
	    	String fileName = "img" + k + ".jpg";
	    	fos = new FileOutputStream(fileName);
	    	long t= System.currentTimeMillis();
	    	long end = t+3000;
	    	while((System.currentTimeMillis() < end) ||(i = inStream.read(b)) != -1){

		    	if(headerFound) fos.write(b, 0, i);
		    	// This locates the end of the header by comparing the current byte as well as the next 3 bytes
		        // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
		        // If the end of the header is reached, the flag is set to true and the remaining data in the
		        // currently buffered byte array is written into the file.
		    	if(b[i-1] ==  10 && b[i-2] ==  13 && b[i-3] ==  10 && b[i-4] ==  13) headerFound = true;
		    }
	    	System.out.println("check");
	    	if (fos 				!= null)  fos.close();
	    }
		 **/




		/**
	    int inputLine = in.read();
		System.out.println(inputLine);
		while(in.ready()){
			inputLine = in.readLine();
			if(inputLine.contains("img")){
				String src = getSrc(inputLine);
				allImage[allImage.length] = src;
			}
			System.out.println(inputLine);

		}	


		// get all images
		String pathImage = changePathFotImage(path);
		for(String img: allImage){
			String newPath = pathImage + img;
			request = getHTTP1Request("GET", newPath, hostServer, portNumber);
			output.println(request);

			// Initialize the streams.
			final FileOutputStream fileOutputStream = new FileOutputStream("image.jpg");
			// Header end flag.
		    boolean headerEnded = false;

		    byte[] bytes = new byte[2048];
		    int length;
		    while ((length = in.read(bytes)) != -1) {
		        // If the end of the header had already been reached, write the bytes to the file as normal.
		        if (headerEnded)
		            fileOutputStream.write(bytes, 0, length);

		        // This locates the end of the header by comparing the current byte as well as the next 3 bytes
		        // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
		        // If the end of the header is reached, the flag is set to true and the remaining data in the
		        // currently buffered byte array is written into the file.
		        else {
		            for (int i = 0; i < 2045; i++) {
		                if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
		                    headerEnded = true;
		                    fileOutputStream.write(bytes, i+4 , 2048-i-4);
		                    break;
		                }
		            }
		        }
		    }
		    fileOutputStream.close();
		}

		 **/

		/*********************************************************************************
		 * 	CONNECTION CLOSED
		 ********************************************************************************/
		// 4 - Close the streams and socket
		stop();
	}


	/*********************************************************************************
	 * 	HELP FUNCTIONS
	 ********************************************************************************/

	public String getHTTP1Request(String method, String path, String host, int portNumber, boolean image){
		String request = "";
		// method = first line of the HTTP request 
		String methodLine = "";
		// host = second line of the HTTP request
		String hostLine= "";
		methodLine =  method+ " " + path + " HTTP/1.1";
		hostLine = "Host:" + host + ":" + portNumber;
		// Form the request
		request = methodLine + '\n' + hostLine + '\r' + '\n';
		return request;
	}

	public String getSrc(String line){
		boolean found = false;
		String[] splitLine = line.split("\"");
		for(String i : splitLine){
			if(found){
				return i;
			}
			if (i.contains("src")){
				found = true;
			}
		}
		return "";
	}

	public String changePathForImage(String path){
		String result = "";
		String[] srcArray = path.split("(?<=/)");
		for(int i = 0; i < (srcArray.length -1); i++){
			result = result + srcArray[i];
		}
		return result;
	}

	public void downloadImage(String src, String host, String path, int portNumber) throws FileNotFoundException{

	}

	/**
	 * Close the connection
	 */
	public void stop(){  
		try{   
			if (output 			!= null)  output.close();
			if (inStream 		  		!= null)  inStream.close();
			if (clientSocket    != null)  clientSocket.close();
			//if (writer			!= null) writer.close();
		}
		catch(IOException ioe){ 
			System.out.println("Error closing ...");
		}
	}


	/*********************************************************************************
	 * 	MAIN 
	 *******************************************************************************/

	/**
	 * @throws Exception 
	 */
	public static void main(String args[]) throws Exception{  
		ChatClient chatClient = null;
		URI uri = new URI(args[1]);
		try{
			chatClient = new ChatClient(args[0],uri, Integer.parseInt(args[2]));
		}
		//If the port number is not given then the default port is 80
		catch(IndexOutOfBoundsException exc)
		{
			chatClient = new ChatClient(args[0],uri, 80);
		}
	}
}