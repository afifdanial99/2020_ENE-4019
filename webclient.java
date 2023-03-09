package webclient;

import java.io.* ; 
import java.net.* ; 
import java.util.* ;

public class webclient {
	
	public String getWebContentByGet(String urlString, final String charset, int timeout) throws IOException {
		if (urlString == null || urlString.length() == 0) {
			return null;
		}
		
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString
				: ("http://" + urlString).intern();
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		
		conn.setRequestProperty("User-Agent", "2019048586/AFIFDANIAL/WebClient/Comnet");
		
		conn.setRequestProperty("Accept", "text/html");
		conn.setConnectTimeout(timeout);
		
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		InputStream input = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		if (reader != null) {
			reader.close();
		}
		if (conn != null) {
			conn.disconnect();
		}
		return sb.toString();
		
	}
	

	public String getWebContentByPost(String urlString, String data, final String charset, int timeout) throws IOException {
	
		if(urlString == null || urlString.length() == 0) {
			return null;
		}
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString
				: ("http://" + urlString).intern();
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		
		connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		
		connection.setRequestProperty("User-Agent", "2019048586/AFIFDANIAL/WebClient/Comnet");
		
		connection.setRequestProperty("Accept", "text/xml");
		connection.setConnectTimeout(timeout);
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		
		byte[] content = data.getBytes("UTF-8");
		
		out.write(content);
		out.flush();
		out.close();
		
		try {
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK ) {
				return null;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\r\n");
		}
		if (reader != null) {
			reader.close();
		}
		if (connection != null) {
			connection.disconnect();
		}
		return sb.toString();
	}
	
	public String getWebContentByPost(String urlString,String data) throws IOException {  
		return getWebContentByPost(urlString, data,"UTF-8", 5000);//iso-8859-1  
	}  
	
	public String getWebContentByGet(String urlString) throws IOException {  
		return getWebContentByGet(urlString, "iso-8859-1", 5000);  
	}
	
	public static void main(String[] args) throws IOException{
		webclient client=new webclient();

        Scanner url = new Scanner(System.in);
        System.out.println("Insert URL: ");
        String givenUrl = url.nextLine();
        String s = client.getWebContentByGet(givenUrl); 
        System.out.println("Received Message: ");
        System.out.println(s);

        Scanner giveUrl = new Scanner(System.in);
        System.out.println("Insert URL: ");
        String urlpic = giveUrl.nextLine();

        Scanner ans = new Scanner(System.in);
        System.out.println("Insert data: ");
        String data = ans.nextLine();
        s = client.getWebContentByPost(urlpic,data);
        System.out.println(s);
	}
}
	

