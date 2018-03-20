import java.net.*;
import java.nio.charset.StandardCharsets;
import java.awt.Image;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

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
	 * inStream: reads the response of the server
	 * output: sends data through the socket to the server
	 * writer: writes data to the file file.html so it is saved
	 * //TODO private of public zetten

	 * fos: writes data to a file
	 * allImages: contains all sources of the images that the html file contains
	 */

	Socket clientSocket = null;
	InputStream inStream = null;
	InputStreamReader inImage = null;
	PrintWriter output = null;
	FileOutputStream fos = null;
	List<String> allImages = new ArrayList<String>();


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
					System.out.println('\n');
					System.out.println(newRequest);
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
	 * Reads the response of the server and saves the response in a seperate file.
	 * @param HTTPMethod:
	 * 			Method used in the HTTP request
	 * @param imageNum
	 * 			== -1 if no images are asked
	 * 			>=0 if it is a request for a images that should be saved in "img<imageNum>.<type>"
	 * @param type
	 * 		type is given if the request is for an image, otherwise type == ""
	 * @throws IOException
	 */
	public void readResponse(String HTTPMethod) throws IOException{
		/* Open all connection to read the response */
		try{
			fos = new FileOutputStream("file.html"); //file to store the data
			inStream = clientSocket.getInputStream(); // start an InputStream
		}catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}

		/*
		 * Retrieve the input in blocks of 1kb.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		byte[] b = new byte[1024]; // 1kb reading blocks.
		int i = 0;
		boolean next = true;
		int contentLength = 0;// keeps up how much there has bean read
		boolean headerEnded = false;
		int CONTENTLENGTH = 0;// Will be the content length of the total body
		/* Read the input of server and write to file.html */
		try{
			while(next){
				/* Reads i bytes from the input stream and stores it 
				 * into the buffer array b.
				 */
				i = inStream.read(b);
				if(i == -1){ // return is -1 if the stream ended
					break;
				}
				String str = new String(b, StandardCharsets.UTF_8);// transform to string
				//System.out.print(str);
				if(!headerEnded){
					if((CONTENTLENGTH = getContentLength(str))==0){
						System.out.println('\n' + "HTTP response does not contain Content-Length!");
						break;
					}
					for (int k = 0; k < 1024; k++) {
						// This locates the end of the header by comparing the current byte as well as the next 3 bytes
						// with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
						// If the end of the header is reached, the flag is set to true and the remaining data in the
						// currently buffered byte will be counted with the content length that has already been read.
						if (b[k] == 13 && b[k+ 1] == 10 && b[k + 2] == 13 && b[k + 3] == 10) {
							headerEnded = true;
							contentLength += i-k-3;
							if(HTTPMethod.equals("HEAD")) next = false;
							break;

						}
					}
				}
				else{
					contentLength += i;
					// If the last byte read is equal to 10 (or '\n') and the content length has
					// been reached then the input stream has reached the end of the response.
					// The flag 'next' is set to false to stop reading.
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

		// Close all open connections made
		try{
			if (fos 				!= null)  fos.close();
			//if (inStream 			!= null)  inStream.close();
		}
		catch(IOException ioe){ 
			System.out.println("Error closing ...");
		}

	}

	public void downloadImage(int imageNum,String type) throws IOException{
		String fileName = "image" + imageNum + "." + type;	
		OutputStreamWriter imageFile = null;
		try {
			inImage = new InputStreamReader(clientSocket.getInputStream(),"ISO-8859-1");
			imageFile = new OutputStreamWriter(new FileOutputStream(fileName),"ISO-8859-1");
		}catch(IOException ioe){  
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
		/*
		 * Retrieve the input in blocks of 1kb.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		List<Integer> last4Readings = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			last4Readings.add(0);
		}
		int ch = 0;
		Integer integer = null;
		boolean next = true;
		boolean headerEnded = false;
		/* Read the input of server and write to file.html */
		try{
			while(next){
				/* Reads i bytes from the input stream and stores it 
				 * into the buffer array b.
				 */
				ch = inImage.read();
				if(ch == -1){ // return is -1 if the stream ended
					System.out.print('\n'+"quit");
					break;
				}
				integer = new Integer(ch);
				last4Readings.add(integer);
				last4Readings.remove(0);

				if(!headerEnded){
					// This locates the end of the header by comparing the current byte as well as the next 3 bytes
					// with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
					// If the end of the header is reached, the flag is set to true and the remaining data in the
					// currently buffered byte will be counted with the content length that has already been read.
					if(last4Readings.get(0)==13 && last4Readings.get(1)==10 && last4Readings.get(2)==13 && last4Readings.get(3)==10){
						headerEnded = true;
						System.out.print('\n'+"check Header");
					}
				}
				else{
					imageFile.write(ch);
				}
			}
		}catch(IOException exc){
			System.out.println('\n'+ "Could not download the image." + "\n");
		}

		// Close all open connections made
		try{
			if (inImage 				!= null) inImage.close();
			if (imageFile 				!= null) imageFile.close();

		}
		catch(IOException ioe){ 
			System.out.println("Error closing ...");
		}
	}


	/*********************************************************************************
	 * 	HELP FUNCTIONS
	 ********************************************************************************/

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
			if (clientSocket    != null)  clientSocket.close();
			if (inStream 			!= null)  inStream.close();
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