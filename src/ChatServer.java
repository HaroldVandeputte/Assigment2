import java.net.*;
import java.io.*;

public class ChatServer
{  private Socket          socket   = null;
   private ServerSocket    server   = null;
   private DataInputStream streamIn =  null;

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
		   DataInputStream streamIn = open(socket);
		   
		   boolean done = false;
		   while (!done) {  
			   try {  
				   System.out.println("Print voor readUTF");
				   String line = streamIn.readUTF();
				   System.out.println("print na UTF");
				   System.out.println(line);
				   try (Writer request = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("request.txt"), "utf-8"))) {
				   request.write(line + '\n');
				}
				   done = line.equals(".bye");
			   	}
			   catch(IOException ioe) {  
				   done = true;
			   }
		   }
		   System.out.println("Close!");
		   close();
		   }
	   catch(IOException ioe) {  
		   System.out.println(ioe); 
	   }
   }
   
   public void readInput() {
	   
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
	   if (streamIn != null)  streamIn.close();
   }
   
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