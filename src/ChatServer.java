import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ChatServer {  
	private Socket          socket   = null;
	private ServerSocket    server   = null;
	private BufferedReader 	input 	 = null;
	private PrintWriter     output   = null;
	List<String> allFiles = new ArrayList<String>();
	FileOutputStream fos = null;
	Boolean badRequest = false;
	int port = 0;
	
	/**
	 * Initialize a Chatserver at a port.
	 * 
	 * @param port
	 */
	public ChatServer(int portInput) {  
		allFiles.add("Assignment2.html");
		allFiles.add("example.html");
		
		
		try {
			this.port = portInput;
			server = new ServerSocket(port);  
			System.out.println("Connection made.");
			socket = server.accept();
			System.out.println("Client accepted");
			System.out.println("-------------------------");
			input = new  BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			PrintWriter request = new PrintWriter("request.txt", "utf-8");
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";

			boolean done = false;
			while (!done) {  
				try {  
					line1 = input.readLine();
					System.out.println("MethodLine: " + line1);
					request.println(line1);
					line2 = input.readLine();
					System.out.println("Hostline: " + line2);
					request.println(line2);
					line3 = input.readLine();
					System.out.println(line3);
					System.out.println(badRequest);
					if (line3.equals("")) {
						System.out.println("Line3: " + line3);
						done = true;
						if (badRequest) returnStatusCode(badRequest400());
						readInput(line1, line2, null, null);
					}
					else {
						line4 = input.readLine();
						System.out.println(line4);
						String line = input.readLine();
						System.out.println(line);
						if (!line.equals("")) badRequest = true;
						System.out.println(badRequest + "lege lijn");
						done = true;
						readInput(line1, line2, line3, line4);
					}
					System.out.println(badRequest);
					
					request.close();
				}
				catch(IOException ioe) {  
					done = true;
				}
			}
			
			
			if (badRequest) returnStatusCode(badRequest400());
			String fileName = splitURI();
			if (badRequest) returnStatusCode(badRequest400());
			else {
				switch (HTTPcommand) {
				case("GET"):
					GETfunction(fileName);
					break;
				case("HEAD"):
					HEADfunction(fileName);
					break;
				case("PUT"):
					PUTfunction();
					break;
				case("POST"):
					POSTfunction();
					break;
				default: notImplemented501();
				}
			}
			
			System.out.println("Close socket and stream.");
			close();
		}
		catch(IOException ioe) {  
			System.out.println(ioe); 
		}
	}

	private void returnStatusCode(String statusCode) {
		output.println(statusCode);
		//System.out.println("flikker ts BAD--------------------------------------------------------------");
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	String HTTPcommand = "";
	String URI = "";
	String HTTPversion = "";
	String Host = "";
	int portSend = 0;
	int contentLength = 0;
	public void readInput(String line1, String line2, String line3, String line4) {
		String[] array = line1.split(" ");
		HTTPcommand = array[0];
		URI = array[1];
		HTTPversion = array[2];
		System.out.println(badRequest);
		if (!HTTPversion.equals("HTTP/1.1")) {
			badRequest = true;
			return;
		}
		String[] array2 = line2.split(":");
		System.out.println(badRequest);
		if (!array2[0].equals("Host")) {
			badRequest = true;
			return;
		}
		System.out.println(badRequest);
		Host = array2[1];
		portSend = Integer.parseInt(array2[2]);
		if (!Host.equals("localhost")) {
			badRequest = true;
			return;
		}
		if (portSend != port) {
			badRequest = true;
			return;
		}
		System.out.println(badRequest);
		if (line3 == null) return;
		String[] array3 = line3.split(" ");
		if (!array3[0].equals("Content-Length:")) {
			badRequest = true;
			return;
		}
		contentLength = Integer.parseInt(array3[1]);
		
		if (!line4.equals("Content-Type: text/txt")) badRequest = true;
		//System.out.println(HTTPcommand + " " + URI + " " + HTTPversion + " " + Host + " " + badRequest);
	}
	
	
	public String splitURI() {
		String file = "";
		String[] array = URI.split("/");
		file = array[array.length - 1];
		return file;
	}
	
	/**
	 * Opens an input stream to the socket.
	 * @throws IOException
	 */
	public DataInputStream open(Socket socket) throws IOException {  
		return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	
	/**
	 * Closes all connections.
	 * @throws IOException
	 */
	public void close() throws IOException {  
		if (socket != null)    socket.close();
		if (input != null)  input.close();
	}

	/*************************************************************
	 * HTTPcommands
	 *************************************************************/

	private void GETfunction(String file) {
		System.out.println("GET");
		//System.out.println(file);
		if (!checkFileSystem(file)) {
			returnStatusCode(notFound404());
			return;
		}
		int contentLength = getContentLength(file);
		//System.out.println("contentLength = " + contentLength);
		String header = getHeader(ok200(), contentLength);
		
		output.println(header);
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
	    String s = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
	    output.println(s);
	    
	    try {
			in.close();
			
		} catch (IOException e) {
			
		}
	}
	
	public boolean checkFileSystem(String file) {
		if (!allFiles.contains(file)) return false;
		else return true;
	}
	
	public int getContentLength(String file) {
		//System.out.println(file);
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
		int i;
		//System.out.println(in);
		byte[] b = new byte[1];
		int contentLength = 0;
		try {
			while ((i = in.read(b)) != -1){
				contentLength += i;
			}
		} catch (IOException e) {
			//System.out.println("lulletje");
		}
		try {
			in.close();
		} catch (IOException e) {
			//System.out.println("lulletje2");
		}
		return contentLength;
	}

	private void HEADfunction(String file) {
		//System.out.println("HEAD");
		//System.out.println(file);
		//System.out.println(badRequest);
		if (!checkFileSystem(file)) {
			returnStatusCode(notFound404());
			return;
		}
		int contentLength = getContentLength(file);
		System.out.println("contentLength = " + contentLength);
		String header = getHeader(ok200(), contentLength);
		output.println(header);		
	}

	public void readResponse(String HTTPMethod, int CONTENTLENGTH) throws IOException{
		/**
		 * Retrieve the input line per line and build the response.
		 * Print the response in the terminal
		 * Save the response in the file : file.html
		 */
		byte[] b = new byte[1024]; // 1kb reading blocks.
		InputStream inStream = socket.getInputStream();
		//input.close();
		fos = new FileOutputStream("testje.txt"); //file to store the data
		int i = 0;
		boolean next = true;
		int contentlength = 0;
		boolean headerEnded = false;
		/* Read the input of server and write to file.html */
		try{
			while(next){
				/* Reads 1 byte from the input stream and stores it 
				 * into the buffer array b.
				 */
				i = inStream.read(b);
				System.out.println(i);
				
				if(!headerEnded){
					{
						for (int k = 0; k < 1024; k++) {
							if (b[k] == 13 && b[k+ 1] == 10 && b[k + 2] == 13 && b[k + 3] == 10) {
								headerEnded = true;
								contentlength += i-k-3;
								break;
							}
						}
					}
				}else{
					contentlength += i;
					fos.write(b, 0, i);
					if(b[i-1]==10 && contentlength >= CONTENTLENGTH){
						next = false;
					}
				}
			}
		}catch(IOException exc){
			String str = new String(b, StandardCharsets.UTF_8);// transform to string
			System.out.print(str);
			fos.write(b, 0, i);
		}
		if (fos 				!= null)  fos.close(); // Close the stream for later uses

	}
	
	
	private void PUTfunction() {
		System.out.println("PUT");
		try {
			readResponse("PUT", contentLength);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void POSTfunction() {
		System.out.println("POST");

	}

	
	/*************************************************************
	 * STATUS CODES
	 *************************************************************/

	public String ok200() {
		return ("HTTP/1.1 200 OK");
	}

	public String notModified304() {
		return ("HTTP/1.1 304 Not Modified");
	}

	public String badRequest400() {
		return ("HTTP/1.1 400 Bad Request");
	}

	public String notFound404() {
		return ("HTTP/1.1 404 Not Found");
	}

	public String serverError500() {
		return ("HTTP/1.1 500 Server Error");
	}

	public String notImplemented501() {
		return ("HTTP/1.1 501 Not Implemented");
	}
	
	/*************************************************************
	 * HEADER
	 *************************************************************/
	
	public String getHeader(String statusCode, int contentLength) {
		Date date = new Date();
		
		Locale localeEN = Locale.ENGLISH;
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", localeEN);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateGMT = df.format(date);
		
		//System.out.println(dateGMT);
		String header = statusCode + '\n' + "Content-Type: text/html" + '\n' + "Content-Lenght: " + contentLength + '\n' + "Date: " + dateGMT.toString();
		
		return header;
	}
	
	/*************************************************************
	 * MAIN
	 *************************************************************/

	/**
	 * The main class.
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length != 1) System.out.println("Usage: java ChatServer port");
		else new ChatServer(Integer.parseInt(args[0]));
	}
}
