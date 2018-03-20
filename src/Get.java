
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
 * This server class responds to the get request of the client. It retrieves the requested page.
 * 
 */
public class Get {
	/**
	 * This function retrieves the requested html file.
	 * 
	 * @param clientSocket
	 * 			The socket where the server is connected to.
	 * @param inFromClient
	 * 			The buffered inputstream which the server receives from the client.
	 * @param path
	 * 			The path of the file which has to be retrieved.
	 * @throws IOException
	 */
	public static void get(Socket clientSocket, BufferedReader inFromClient, PrintWriter out, String path) throws IOException{

		if (path.isEmpty()){
			path = "index.html";
		}

		//Date GMT
		Date date = new Date();
		Locale localeEN = Locale.ENGLISH;
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", localeEN);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateGMT = df.format(date);

		String[] array = path.split(".");
		String extension = array[array.length-1];

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

				//file uitlezen en doorsturen
				String t;
				while ((t = htmlFile.readLine()) != null) {
					System.out.println(t);
					out.println(t);
					out.flush();
				}
				htmlFile.close();

			}catch(Exception e){
				//page not found
				System.out.println("404");
				out.println("HTTP/1.1 404 Not Found");
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();
			}
		}


	}
}