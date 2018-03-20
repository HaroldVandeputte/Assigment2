import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * This server class responds to the post request of the client. It listens to the given text and saves it in the specified file.

 */
public class Post {
	/**
	 * This function listens to the given text and saves it in the specified file.
	 * 
	 * @param inFromClient
	 * 			The buffered inputstream which the server receives from the client.
	 * @param path
	 * 			The path of the file which has to be retrieved.
	 * @throws IOException
	 */
	public static void post(BufferedReader inFromClient, String path) throws IOException{

		//if path not specified, return index page.
		if (path.isEmpty()){
			path = "post.txt";
		}

		//info client uitlezen
		String s;
		FileWriter fw = new FileWriter(path, true);
		BufferedWriter out = BufferedWriter(fw);
		
		while ((s = inFromClient.readLine()) != null) {
			System.out.println(s);
			out.write(s);
			if (s.isEmpty()) {
				break;
			}
		}

		
	}
}