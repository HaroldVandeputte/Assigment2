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
	 */ //TODO private of public zetten
	Socket clientSocket = null;
	InputStream inStream = null;
	//BufferedReader inBuf = null;
	PrintWriter output = null;
	FileOutputStream fos = null;
	List<String> allImages = new ArrayList<String>();

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
		System.out.println(hostServer);
		String path = uri.getPath();
		int portNumber = port;
		String HTTPMethod = HTTPcommand;
		String body = "";


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
			request = getHTTP1Request("GET", path, hostServer, portNumber, "");
		break;
		case("HEAD"):
			request = getHTTP1Request("HEAD", path, hostServer, portNumber, "");
		break;
		case("PUT"):
			body = getUserInput();
		request = getHTTP1Request("PUT", path, hostServer, portNumber, body);
		break;
		case("POST"):
			body = getUserInput();
		request = getHTTP1Request("POST", path, hostServer, portNumber, body);
		break;
		default: throw new IllegalArgumentException("Invalid HTTP method (for this assignment)" + HTTPMethod);
		}


		/*********************************************************************************
		 * 	READ AND WRITE
		 ********************************************************************************/		

		/* 3 - Read from and write to the stream according to the server's protocol. */
		/* Send to server */
		output.println(request);
		/* Retrieve from server */
		readResponse(HTTPMethod, -1,  true);

		/* If there are images then send request for them again */
		String pathImage = changePathForImage(path);
		for(int k = 0; k < allImages.size(); k++){
			String newPath = pathImage + allImages.get(k);
			String newRequest = getHTTP1Request("GET",newPath,hostServer,portNumber,"");
			output.println(newRequest);
			readResponse("GET",k,false);

		}


		/*********************************************************************************
		 * 	CONNECTION CLOSED
		 ********************************************************************************/
		// 4 - Close the streams and socket
		stop();
	}


	/*********************************************************************************
	 * 	Read Response
	 ********************************************************************************/

	public void readResponse(String HTTPMethod, int imageNum, boolean findImages) throws IOException{
		/**
		 * Retrieve the input line per line and build the response.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		byte[] b = new byte[1024]; // 1kb reading blocks.
		if(imageNum != -1){
			fos = new FileOutputStream("image" + imageNum + ".jpg");
		}else fos = new FileOutputStream("file.html"); //file to store the data
		int i = 0;
		boolean next = true;
		int contentLength = 0;
		boolean headerEnded = false;
		int CONTENTLENGTH = 0;
		/* Read the input of server and write to file.html */
		try{
			while(next){
				/* Reads 1 byte from the input stream and stores it 
				 * into the buffer array b.
				 */

				i = inStream.read(b);
				String str = new String(b, StandardCharsets.UTF_8);// transform to string
				if(imageNum == -1) System.out.print(str);
				/* Looking for images */
				if(str.contains("img") && findImages && HTTPMethod.equals("GET")){
					String src = getSrc(str);
					if(!src.equals(" ")) allImages.add(src);
				}
				if(!headerEnded){
					{
						CONTENTLENGTH = getContentLength(str);
						for (int k = 0; k < 1024; k++) {
							if (b[k] == 13 && b[k+ 1] == 10 && b[k + 2] == 13 && b[k + 3] == 10) {
								headerEnded = true;
								contentLength += i-k-3;
								if(imageNum >=0) b = Arrays.copyOfRange(b, k+3, b.length-1);
								break;
							}
						}
						if(HTTPMethod.equals("HEAD")) next = false;
					}
				}else{
					contentLength += i;
					if(b[i-1]==10 && contentLength >= CONTENTLENGTH){
						next = false;
					}
				}

				fos.write(b, 0, i);
			}
		}catch(IOException exc){
			String str = new String(b, StandardCharsets.UTF_8);// transform to string
			System.out.print(str);
			fos.write(b, 0, i);
		}
		if (fos 				!= null)  fos.close(); // Close the stream for later uses

	}


	/*********************************************************************************
	 * 	HELP FUNCTIONS
	 ********************************************************************************/

	
	public String getUserInput() throws IOException{
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		String body = "";
		System.out.println("Give input for the body, write 'bye' to stop:  " + '\n');
		input = userInput.readLine();
		while(!input.equals("bye")){
			body += input + '\n';
			input = userInput.readLine();
		}
		userInput.close();
		return body;
	}
	
	/**
	 * Form the HTTP request that forfills version 1.1
	 * @param method
	 * 			
	 * @param path
	 * @param host
	 * @param portNumber
	 * @param body
	 * @return
	 */
	public String getHTTP1Request(String method, String path, String host, int portNumber, String body){
		// resulting request
		String request = "";
		// method = first line of the HTTP request 
		String methodLine = "";
		// host = second line of the HTTP request
		String hostLine= "";
		// Content-Length = how many bytes the body contains
		String contentLength = "Content-Length: ";
		// Content-Type = type of the content
		String contentType = "Content-Type: text/txt";
		methodLine =  method+ " " + path + " HTTP/1.1";
		hostLine = "Host:" + host + ":" + portNumber;
		// Form the request
		if(method.equals("GET") || method.equals("HEAD")){
			request = methodLine + '\n' + hostLine + '\r' + '\n';
		}else{
			int amountOfBytes = body.getBytes().length;
			contentLength += amountOfBytes;
			request = methodLine + '\n' + hostLine + '\n' + contentLength +'\n' + 
					contentType + '\r' + '\n' + '\r' + '\n' + body;
		}
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
		return " ";
	}

	public String changePathForImage(String path){
		String result = "";
		String[] srcArray = path.split("(?<=/)");
		for(int i = 0; i < (srcArray.length -1); i++){
			result = result + srcArray[i];
		}
		return result;
	}

	public int getContentLength(String str){
		String result = "";
		boolean found = false;
		String[] strArray = str.split(" ", 40);
		for(String i : strArray){

			if(found){
				byte[] bytes = i.getBytes();
				for(byte b :bytes){
					if(b == 13){
						return Integer.parseInt(result);
					}
					byte[] subresult = {b};
					result = result+ new String(subresult, StandardCharsets.UTF_8);
				}
			}
			if(i.contains("Content-Length:")) found = true;
		}
		return 0;
	}

	/**
	 * Close the connection
	 */
	public void stop(){  
		try{   
			if (output 			!= null)  output.close();
			if (inStream 		!= null)  inStream.close();
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