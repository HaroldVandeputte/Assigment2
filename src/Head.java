import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
/**
 * This server class responds to the head request of the client. It retrieves the header of the requested page.
 * 
 */
public class Head {
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
	public static void head(Socket clientSocket, BufferedReader inFromClient, PrintWriter out, String path) throws IOException{

		String statusCode = "";

		//if path not specified, return index page.
		if (path.isEmpty()){
			path = "index.txt";
		}


		//Date GMT
		Date date = new Date();
		Locale localeEN = Locale.ENGLISH;
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", localeEN);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateGMT = df.format(date);

		int indexPoint = path.lastIndexOf(".");
		String extension = path.substring(indexPoint+1, path.length());

		if (extension.equals("html")) {
			try{
				File file = null;
				file = new File(path);
				BufferedReader htmlFile = new BufferedReader(new FileReader(file));

				//headers
				out.println("HTTP/1.1 200 OK");
				out.println("Content-Type: text/html");
				out.println("Content-Length: "+file.length());
				out.println("Date: "+ dateGMT);
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();

				htmlFile.close();

			}catch(Exception e){
				//page not found
				System.out.println("404");
				out.println("HTTP/1.1 404 Not Found");
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();
			}
		}	}
}