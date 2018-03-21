import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
/**
 *This server class responds to the put request of the client. It listens to the given text and saves it in the specified file.
 *
 */
public class Put {
	/**
	 * This function listens to the given text and saves it in the specified file.
	 * 
	 * @param inFromClient
	 * 			The buffered inputstream which the server receives from the client.
	 * @param path
	 * 			The path of the file which has to be retrieved.
	 * @throws IOException
	 */
	public static void put(BufferedReader inFromClient, String path) throws IOException{

		//if path not specified, return index page.
		if (path.isEmpty()){
			path = "put.txt";
		}

		//info client uitlezen
		String s;
		PrintWriter out = new PrintWriter(path);

		while ((s = inFromClient.readLine()) != null) {
			System.out.println(s);
			out.println(s);
			out.flush();
			if (s.isEmpty()) {
				break;
			}
		}
		
		

		out.close();
	}
}