import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ChatServer {  
	private Socket          socket   = null;
	private ServerSocket    server   = null;
	private BufferedReader 	input 	 = null;
	private PrintWriter     output   = null;

	Boolean badRequest = false;
	/**
	 * Initialize a Chatserver at a port.
	 * 
	 * @param port
	 */
	public ChatServer(int port) {  
		try {
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

			boolean done = false;
			while (!done) {  
				try {  
					line1 = input.readLine();
					//System.out.println(line1);
					request.println(line1);
					line2 = input.readLine();
					//System.out.println(line2);
					request.println(line2);
					String line = input.readLine();
					done = line.equals("");
					if (!line.equals("")) badRequest = true;
					done = true;
					request.close();
				}
				catch(IOException ioe) {  
					done = true;
				}
			}

			returnBadRequest();

			readInput(line1, line2);

			returnBadRequest();

			switch (HTTPcommand) {
			case("GET"):
				GETfunction();
				break;
			case("HEAD"):
				HEADfunction();
				break;
			case("PUT"):
				PUTfunction();
				break;
			case("POST"):
				POSTfunction();
				break;
			default: notImplemented501();
			}


			System.out.println("Close socket and stream.");
			close();
		}
		catch(IOException ioe) {  
			System.out.println(ioe); 
		}
	}

	private void returnBadRequest() {
		if (badRequest) {
			output.println(badRequest400());
		}
	}


	String HTTPcommand = "";
	String URI = "";
	String HTTPversion = "";
	String Host = "";
	public void readInput(String line1, String line2) {
		String[] array = line1.split(" ");
		HTTPcommand = array[0];
		URI = array[1];
		HTTPversion = array[2];
		if (!HTTPversion.equals("HTTP/1.1")) badRequest = true;
		String[] array2 = line2.split(":");
		if (!array2[0].equals("Host")) badRequest = true;
		Host = array2[1];
		//System.out.println(HTTPcommand + " " + URI + " " + HTTPversion + " " + Host + " " + badRequest); TODO Host is nog null
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

	private void GETfunction() {
		//System.out.println("GET");
		String header = getHeader(ok200());
		output.println(header);
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("Assignment2.html");
	    String s = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
	    output.println(s);
	}

	private void HEADfunction() {
		System.out.println("HEAD");

	}

	private void PUTfunction() {
		System.out.println("PUT");

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
	
	public String getHeader(String statusCode) {
		Date date = new Date();
		
		Locale localeEN = Locale.ENGLISH;
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", localeEN);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateGMT = df.format(date);
		
		//System.out.println(dateGMT);
		String header = statusCode + '\r' + '\n' + "Date: " + dateGMT.toString() + '\n' + "Content-Type: text/html" + '\n' + "Content-Lenght: 24557";
		
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
		ChatServer server = null;
		if (args.length != 1) System.out.println("Usage: java ChatServer port");
		else server = new ChatServer(Integer.parseInt(args[0]));
	}
}
