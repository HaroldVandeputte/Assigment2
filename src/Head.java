package Server;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
/**
 * This server class responds to the head request of the client. It retrieves the header of the requested page.
 * 
 * @author Laura Vranken
 * @author Arne Vlietinck
 *
 */
public class HeadServer {
	/**
	 * This function retrieves the header of the requested page.
	 * 
	 * @param clientSocket
	 * 			The socket where the server is connected to.
	 * @param inFromClient
	 * 			The buffered inputstream which the server receives from the client.
	 * @param path
	 * 			The path of the file which has to be retrieved.
	 * @throws IOException
	 */
	public static void head(Socket clientSocket, BufferedReader inFromClient, PrintWriter out, String path, String http) throws IOException{

		String statusCode = "";

		//if path not specified, return index page.
		if (path.isEmpty()){
			path = "index.txt";
		}

		boolean noHeader =false;
		//info client uitlezen
		String s;
		while ((s = inFromClient.readLine()) != null) {
			noHeader = true;
			System.out.println(s);
			if (s.isEmpty()) {
				break;
			}
		}
				
		if(noHeader == false && http == "1.0"){
			out.println("HTTP/1.0 400 Bad Request");
		}

		//file uitlezen
		Date today = new Date(); 
		try{
			File file = new File(path);
			statusCode = "200 OK";

			//headers doorsturen
			out.println("HTTP/"+http+" "+statusCode);
			out.println("Content-Type: text/html");
			out.println("Content-Length: "+file.length());
			out.println("Date: "+today);
			out.println("is-modiefied-since: " + file.lastModified());
			out.println("");
			out.flush();

		}catch(Exception e){
			//page not found
			statusCode = "404 Not Found";
			out.println("HTTP/"+http+ " "+statusCode);
			out.println("");
			out.flush();
		}
	}
}