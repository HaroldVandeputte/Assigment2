
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.imageio.ImageIO;

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
				out.println("Content-Length: " + (file.length() - 19));
				out.println("Date: "+ dateGMT);
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();	

				//file uitlezen en doorsturen
				String t;
				while ((t = htmlFile.readLine()) != null) {
					out.println(t);
					out.flush();
				}
				htmlFile.close();

			}catch(Exception e){
				System.out.println("404");
				out.println("HTTP/1.1 404 Not Found");
				out.println('\r' + '\n' + '\r' + '\n');
				out.flush();
			}
		}
		else if (extension.equals("jpg")){
			
//			BufferedImage image = ImageIO.read(new File(path));
//
//	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//	        ImageIO.write(image, "jpg", byteArrayOutputStream);
//
//	        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
//	        
//	      //headers
//			out.println("HTTP/1.1 200 OK");
//			out.println("Content-Type: text/html");
//			out.println("Content-Length: "+ size);
//			out.println("Date: "+ dateGMT);
//			out.println('\r' + '\n' + '\r' + '\n');
//			out.flush();
//	        
//	        
//	        OutputStream outputStream = clientSocket.getOutputStream();
//	        outputStream.write(size);
//	        outputStream.write(byteArrayOutputStream.toByteArray());
//	        outputStream.flush();
		}


	}
}