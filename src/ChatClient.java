import java.net.*;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author Jonas Wittouck
 * @author Harold Vandeputte
 * @version 1.0
 *
 */
public class ChatClient {


	/*********************************************************************************
	 * 			VARIABLES
	 ********************************************************************************/

	/*
	 * Initialize variables for the connection
	 * 
	 * clientSocket: a connection is made through the socket
	 * input: reads the response of the server
	 * output: sends data through the socket to the server
	 * writer: writes data to the file file.html so it is saved
	 * //TODO private of public zetten

	 * fos: writes data to a file
	 * imageFile: writes data to the image file
	 */

	Socket clientSocket = null;
	InputStreamReader inputStream = null;
	InputStreamReader inputImage = null;
	PrintWriter output = null;
	FileOutputStream fos = null;
	OutputStreamWriter imageFile = null;


	/*********************************************************************************
	 *			CLASS
	 ********************************************************************************/

	private ChatClient(String HTTPcommand, URI uri, int port) throws Exception{

		/*
		 * Initialize variables
		 * 
		 * hostServer = servers identity
		 * path = path of the URI
		 * portNumber = port to with the server/client should listen to
		 * HTTPMethod = HTTP method 
		 * body = body used for PUT and POST HTTP method
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

			// 2 - Open an output stream to the socket.
			output = new PrintWriter(clientSocket.getOutputStream(), true);

		}catch(UnknownHostException uhe){
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}

		// Form the request depended on witch method is given
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
		readResponse(HTTPMethod);
		/* Search for all the sources of the existing images */
		if(HTTPMethod.equals("GET")){
			try {
				//Connect to the website and get the html
				String uriJsoup = uri.toString();
				Document doc = Jsoup.connect(uriJsoup).get();
				//Get all elements with image tag ,
				Elements img = doc.getElementsByTag("img");

				String pathImage = changePathForImage(path);
				int imgNum = 0;
				/* If there are images then send a separate request for them again */
				for (Element el : img) {
					String src = el.absUrl("src");// http://host/path

					//Extract the name of the image from the src attribute
					int indexName = src.lastIndexOf("/");
					if (indexName == src.length()) {
						src = src.substring(0, indexName);
					}
					indexName = src.lastIndexOf("/");
					String name = src.substring(indexName, src.length());
					int indexType = src.lastIndexOf(".");
					String type = src.substring(indexType+1,src.length());
					String newPath = pathImage + name; // form the correct path
					String newRequest = getHTTP1Request("GET",newPath,hostServer,portNumber,"");// form the correct request
					output.println(newRequest); // send request to server
					downloadImage(imgNum,type); // download/save the image
					imgNum ++;
				}
			} catch (IOException ex) {
				System.err.println("There was an error");
			}

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

	/**
	 * Reads the response of the server and saves the response in a separate file.
	 */
	public void readResponse(String HTTPMethod) throws IOException{
		/* Open all connection to read the response */
		try{
			fos = new FileOutputStream("file.html"); //file to store the data
			inputStream = new InputStreamReader(clientSocket.getInputStream(),"ISO-8859-1");
		}catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		int byteRead = 0;
		byte[] b = new byte[1024];
		int count = -1;
		boolean somethingWentWrong = false;
		int countContentLength = 0;
		Integer CONTENTLENGTH = 10000;
		String	CONTENTSTRING = "";
		boolean contentLengthFound = false;
		byte[] contentString = new byte[16];
		String str = "";
		
		boolean headerEnded = false;
		byte[] headerCheck = new byte[4];
		
		while(countContentLength-1 <= CONTENTLENGTH ){
			byteRead = inputStream.read();
			contentString = addChar(contentString, byteRead);
			if(count == 8 && byteRead!= 50 && !headerEnded){
				somethingWentWrong = true;
			}
			if(contentLengthFound){
				if(byteRead == 13){
					contentLengthFound = false;
					CONTENTLENGTH = Integer.parseInt(CONTENTSTRING);
				}
				else {
					CONTENTSTRING += String.valueOf(byteRead-48);
					countContentLength ++;
				}
			}
			if((str = new String(contentString, StandardCharsets.UTF_8)).equals("Content-Length: ")){
				contentLengthFound = true;
			}
			addChar(headerCheck,byteRead);
			if(!headerEnded){
				// This locates the end of the header by comparing the current byte as well as the next 3 bytes
				// with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
				// If the end of the header is reached, the flag is set to true and the remaining data in the
				// currently buffered byte will be counted with the content length that has already been read.
				if(headerCheck[0]==13 && headerCheck[1]==10 && headerCheck[2]==13 && headerCheck[3]==10){
					headerEnded = true;
					if(somethingWentWrong){
						System.out.println('\n' + "SOMETHING WENT WRONG");
						break;
					}
				}
			}
			else{
				countContentLength++;
			}
			count++;
			fos.write(byteRead);
			if((count%1024) == 1023){
				str = new String(b, StandardCharsets.UTF_8);
				System.out.println(str);
			}
			b[count%1024] = (byte) byteRead;
		}
		b = Arrays.copyOfRange(b, 0, (count +1)%1024);
		str = new String(b, StandardCharsets.UTF_8);
		System.out.println(str);
		// Close open connections made in this function
		try{
			if (fos 				!= null) fos.close();
		}
		catch(IOException ioe){ 
			System.out.println("Error closing ..." + ioe.getMessage());
		}
	}

	public void downloadImage(int imageNum,String type) throws IOException{
		String fileName = "image" + imageNum + "." + type;	
		OutputStreamWriter imageFile = null;
		/* Start new reader */
		try{
			inputStream = new InputStreamReader(clientSocket.getInputStream(),"ISO-8859-1");
			imageFile = new OutputStreamWriter(new FileOutputStream(fileName),"ISO-8859-1");
		}catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		
		/*
		 * Retrieve the input in blocks of 1kb.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		String str = "";
		boolean somethingWentWrong = false;
		byte[] headerCheck = new byte[4];
		boolean headerEnded = false;
		int byteRead = 0;
		int countContentLength = 0;
		Integer CONTENTLENGTH = 10000;
		String CONTENTSTRING = "";
		boolean contentLengthFound = false;
		byte[] contentString = new byte[16];
		int count = 0;
		/* Read the input of server and write to file.html */
		try{
			while(countContentLength <= CONTENTLENGTH ){
				byteRead = inputStream.read();
				contentString = addChar(contentString, byteRead);
				if(count == 9 && byteRead!= 50 && !headerEnded){
					somethingWentWrong = true;
				}
				if(contentLengthFound){
					if(byteRead == 13){
						contentLengthFound = false;
						CONTENTLENGTH = Integer.parseInt(CONTENTSTRING);
					}
					else {
						CONTENTSTRING += String.valueOf(byteRead-48);
						countContentLength ++;
					}
				}
				if((str = new String(contentString, StandardCharsets.UTF_8)).equals("Content-Length: ")){
					contentLengthFound = true;
				}
				headerCheck = addChar(headerCheck,byteRead);
				if(!headerEnded){
					// This locates the end of the header by comparing the current byte as well as the next 3 bytes
					// with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
					// If the end of the header is reached, the flag is set to true and the remaining data in the
					// currently buffered byte will be counted with the content length that has already been read.
					if(headerCheck[0]==13 && headerCheck[1]==10 && headerCheck[2]==13 && headerCheck[3]==10){
						headerEnded = true;
					}
					if(somethingWentWrong){
						System.out.println('\n' + "SOMETHING WENT WRONG");
						break;
					}
				}
				else{
					countContentLength++;
					imageFile.write(byteRead);
				}
				count++;
			}
		}catch(IOException exc){
			System.out.println('\n'+ "Could not download the image." + "\n");
		}

		// Close all open connections made
		try{
			if (imageFile 				!= null) imageFile.close();
		}
		catch(IOException ioe){ 
			System.out.println("Error closing ...");
		}
	}


	/*********************************************************************************
	 * 	HELP FUNCTIONS
	 ********************************************************************************/


	public byte[] addChar(byte[] byteArray, int byteRead){
		for(int i= 1; i < byteArray.length; i++){
			byteArray[i-1] = byteArray[i];
		}
		byteArray[byteArray.length-1] = (byte) byteRead;
		return byteArray;
	}


	/**
	 * Gets the users input
	 */
	public String getUserInput() throws IOException{
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		String body = "";
		System.out.println("Give input for the body, write 'bye' to stop:  " + '\n');
		input = userInput.readLine();
		while(!input.equals("bye")){ //stop if user returns "bye"
			body += input + '\n';
			input = userInput.readLine();
		}
		userInput.close();
		return body;
	}

	/**
	 * Form the HTTP request that fulfills version 1.1
	 */
	public String getHTTP1Request(String method, String path, String host, int portNumber, String body){
		// Resulting request
		String request = "";
		// methodLine = first line of the HTTP request 
		String methodLine = "";
		// hostLine = second line of the HTTP request
		String hostLine= "";
		// Content-Length = how many bytes the body contains
		String contentLength = "Content-Length: ";
		// Content-Type = type of the content
		String contentType = "Content-Type: text/plain";
		
		methodLine =  method+ " " + path + " HTTP/1.1";
		hostLine = "Host:" + host + ":" + portNumber;
		// Form the request
		if(method.equals("GET") || method.equals("HEAD")){
			request = methodLine + '\n' + hostLine + '\r' + '\n';
		}else{
			int amountOfBytes = body.getBytes().length;
			contentLength += amountOfBytes;
			request = methodLine + '\n' + hostLine + '\n' + contentLength +'\n' + 
					contentType + '\r' + '\n' + '\r' + '\n' + body; // \r\n\r\n is the end of the header
		}
		return request;
	}

	/**
	 * Looks for the source of the image in the line.
	 */
	public String getSrc(String line){
		boolean found = false;
		String[] splitLine = line.split("\"");//split string where there is a ' " '
		for(String i : splitLine){
			if(found){
				return i;
			}
			if (i.contains("src") || i.contains("SRC")){// if the string is src then return the next string in the row
				found = true;
			}
		}
		return " ";
	}

	/**
	 * Cuts the last piece of the path away so the image-file name can be added later to 
	 * make the request.
	 */
	public String changePathForImage(String path){
		int lastIndex = path.lastIndexOf("/");
		String result = path.substring(0,lastIndex);
		return result;
	}

	/**
	 * Returns the content length in bytes. Return 0 if something went wrong
	 */
	public int getContentLength(String str){
		String result = "";
		boolean found = false;
		String[] strArray = str.split(" ", 40);// splits string with " "
		for(String i : strArray){
			if(found){
				byte[] bytes = i.getBytes();
				for(byte b :bytes){
					if(b == 13){ //if we are in the line of the content length that add the bytes until byte 13(== next line)
						return Integer.parseInt(result);
					}
					byte[] subresult = {b};
					result = result+ new String(subresult, StandardCharsets.UTF_8);// add everything together one by one
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
			if (inputStream 	!= null) inputStream.close();
			if (inputImage 		!= null) inputImage.close();
			if (clientSocket    != null)  clientSocket.close();
		}
		catch(IOException ioe){ 
			System.out.println("Error closing ...");
		}
	}


	/*********************************************************************************
	 * 	MAIN 
	 *******************************************************************************/

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