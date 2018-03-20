import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class starts a server.
 * 
 */
public class Server {

	private static ServerSocket serverSocket = null;
	private static PrintWriter out = null;

	/**
	 * The main function starts the multithreaded server.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		openServerSocket(port);

		while(true){
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				System.out.println("Client accepted");
				new Thread(new WorkerRunnable(clientSocket, port)).start();
			} catch (IOException e) {
				out = new PrintWriter(clientSocket.getOutputStream());
				out.println("HTTP/1.1 500 Server Error");
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();
				e.printStackTrace();
			}
		}
	}

	/**
	 * Make a new server socket on a given port number.
	 */
	private static void openServerSocket(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Server Error");
		}
	}
}



/**
 * Execute the clients request by retrieving it and redirecting to the right class: GET, HEAD, PUT, POST.
 *
 */
class WorkerRunnable implements Runnable{

	private Socket clientSocket = null;
	private BufferedReader inFromClient;
	private PrintWriter out = null;
	private int port = 0;

	/**
	 * Save the client socket.
	 * @param clientSocket
	 * 		|| socket to connect with client.
	 * @throws IOException 
	 */
	public WorkerRunnable(Socket clientSocket, int port) throws IOException{
		this.clientSocket = clientSocket;
		inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));;
		out = new PrintWriter(clientSocket.getOutputStream());
		this.port = port;

	}

	/**
	 * Read the input request from the client, parse it into the HTTP command and path.
	 * The HTTP command will be worked out in one of the specified classes.
	 */
	@Override
	public void run(){
		System.out.println("run");
		try {
			String firstLine = inFromClient.readLine();
			System.out.println("Received: " + firstLine);

			boolean badRequest = false;
			String[] array = firstLine.split(" ");
			String HTTPcommand = array[0];
			String URI = array[1];
			String HTTPversion = array[2];
			String path = "";
			String[] arrayURI = URI.split("/");
			if (arrayURI.length > 1) path = arrayURI[array.length - 1];
			String secondLine = inFromClient.readLine();
			String[] array2 = secondLine.split(":");
			if (!HTTPversion.equals("HTTP/1.1")) {
				badRequest = true;
			}
			if (!array2[0].equals("Host")) {
				badRequest = true;
			}
			String Host = array2[1];
			int portSend = Integer.parseInt(array2[2]);
			if (!Host.equals("localhost")) {
				badRequest = true;
			}
			if (portSend != port) {
				badRequest = true;
			}

			if (badRequest) {
				out.println("HTTP/1.1 400 Bad Request");
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();
			}
			else {

				switch(HTTPcommand){
				//case "HEAD": HeadServer.head(clientSocket, inFromClient, out, path);
				//break;

				case "GET": Get.get(clientSocket, inFromClient, out, path);
				break;

				case "PUT": Put.put(inFromClient, path);
				break;

				case "POST": Post.post(inFromClient, path);
				break;
				default: {
					out.println("HTTP/1.1 501 Not Implemented");
					out.println('\r' + '\n' + '\r' + '\n');
					out.flush();
				}
				}
			}

		}catch (IOException e) {
			e.printStackTrace();
		}


		//run();

		//inFromClient.close();
		//serverSocket.close();
	}
}