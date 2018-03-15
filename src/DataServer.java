import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * found in: http://cs.lmu.edu/~ray/notes/javanetexamples/
 * A TCP server that runs on port 9090.  When a client connects, it
 * sends the client the current date and time, then closes the
 * connection with that client.  Arguably just about the simplest
 * server you can write.
 */
public class DataServer {

    /**
     * Runs the server.
     */
    public static void main(String[] args) throws IOException {
        // maak ServerSocket aan om connectie te kunnen maken (poort 9090)
    	ServerSocket listener = new ServerSocket(9090);
        
    	try {
            while (true) {
            	//Listens for a connection to be made to this socket and accepts it.
                Socket socket = listener.accept();
                try {
                    PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
                } finally {
                	//Closes this connection
                    socket.close();
                }
            }
        }
        finally {
        	//Closes this socket
            listener.close();
        }
    }
}
