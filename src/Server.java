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
	private static String statusCode = "";

	/**
	 * The main function starts the multithreaded server.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		openServerSocket(Integer.parseInt(args[0]));

		while(true){
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				System.out.println("Client accepted");
				new Thread(new WorkerRunnable(clientSocket)).start();
			} catch (IOException e) {
				statusCode = "500 Server Error";
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
			statusCode = "500 Server Error";
			e.printStackTrace();
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

	/**
	 * Save the client socket.
	 * @param clientSocket
	 * 		|| socket to connect with client.
	 * @throws IOException 
	 */
	public WorkerRunnable(Socket clientSocket) throws IOException{
		this.clientSocket = clientSocket;
		inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));;
		out = new PrintWriter(clientSocket.getOutputStream());

	}

	/**
	 * Read the input request from the client, parse it into the HTTP command and path.
	 * The HTTP command will be worked out in one of the specified classes.
	 */
	@Override
	public void run(){
		System.out.println("run");
		String clientSentence = "";
		String http = "";
		try {
			clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);

			//clientSentence parsen:
			int index = clientSentence.indexOf(" ");
			String command = clientSentence.substring(0, index);
			//System.out.println(command);

			//path parsen
			String path;
			int endindex = clientSentence.indexOf(" ", index+1);
			path = clientSentence.substring(index+2,endindex);

			//Http
			int indexSlash = clientSentence.lastIndexOf("/");
			http = clientSentence.substring(indexSlash+1, clientSentence.length());
			//System.out.println(http + "HTTTTTP");

			switch(command){
			//case "HEAD": HeadServer.head(clientSocket, inFromClient, out, path, http);
			//break;

			case "GET": Get.get(clientSocket, inFromClient, out, path, http);
			System.out.println("LIT");
			break;

			//case "PUT": PutServer.put(inFromClient, path, http);
			//break;

			//case "POST": PostServer.post(inFromClient, path, http);
			//break;
			}

		}catch (IOException e) {
			e.printStackTrace();
		}

		
		//run();
		
		//inFromClient.close();
		//serverSocket.close();
	}
}