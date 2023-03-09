package WebServer;

import java.io.*;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringTokenizer;

public class HttpRequest implements Runnable{
    private final String CRLF = "\r\n";
    private final boolean DEBUG;
    private final Socket CLIENT;
    private final String DATE;

    private final String BAD_REQUEST = "400 BAD REQUEST";
    private final String FILE_NOT_FOUND = "404 FILE NOT FOUND";
    private final String OK = "200 OK";
    private final String POST = "201 CREATED"; 


    HttpRequest(Socket client, boolean debug){
        this.CLIENT = client;
        DEBUG = debug;

        // Setting the current date
        Locale localeUS = new Locale("us","US");
        String zone = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("z")).toString();
        DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy H:mm:ss",localeUS);
        DATE = LocalDateTime.now().format(HTTP_DATE_FORMATTER).toString() + " " + zone;
    }


    private void processRequest() throws IOException{

        InputStream is = CLIENT.getInputStream();


        OutputStream os = CLIENT.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String requestLine = br.readLine();


        if(requestLine == null) {
            BADRequest(os);
        }else {
            if (DEBUG) {
                System.out.println("--------------------------------------------------------- DEBUG INFORMATION ---------------------------------------------------------");
                System.out.println("Request line: " + requestLine);

                String headerLine;
                if (br != null) {
                    while ((headerLine = br.readLine()).length() != 0) {
                        System.out.println(headerLine);
                    }
                }
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            }


            StringTokenizer st = new StringTokenizer(requestLine);
            String requestMethod = st.nextToken();
            String fileName = st.nextToken();
            String httpVersion = st.nextToken();

            fileName = "." + fileName;

            if (httpVersion.equals("HTTP/1.0")) {
                BADRequest(os);
            } else {
                switch (requestMethod) {
                    case "GET":
                        GETRequest(os, fileName);
                        break;
                    case "HEAD":
                        HEADRequest(os, fileName);
                        break;
                    case "POST":
                        POSTRequest(os, br);
                        break;
                    default:
                        BADRequest(os);
                }
            }
        }
        os.close();
        br.close();
        if(DEBUG){
            System.out.println("Closing client: " +
                    CLIENT.getInetAddress() + " " + CLIENT.getPort() +
                    "\n\n");
        }
        CLIENT.close();
    }


    private void POSTRequest(OutputStream os, BufferedReader br) throws IOException{
        br.readLine(); 
        String response = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Post response</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <p>Successfully created your POST request</p>\n" +
                "  </body>\n" +
                "</html>\n";
        sendHeader(os, POST, "text/html", response.getBytes().length);
        os.write(response.getBytes());
        os.flush();
    }


    private void HEADRequest(OutputStream os, String fileName) throws IOException{
        File f = new File(fileName);
        if(!f.exists()){
            String response = "<!DOCTYPE html>\n" +
                    "<HTML>\n" +
                    "  <HEAD>\n" +
                    "    <TITLE>Not Found</TITLE>\n" +
                    "  </HEAD>\n" +
                    "  <BODY>\n" +
                    "    404 Not Found\n" +
                    "  </BODY>\n" +
                    "</HTML>";
            sendHeader(os, FILE_NOT_FOUND, "text/html", response.getBytes().length);
        }else{
            long fileSize = f.length();
            sendHeader(os, OK, contentType(fileName), fileSize);
        }
    }


    private void BADRequest(OutputStream os) throws IOException{
        String response = "<!DOCTYPE html>\n" +
                "<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Bad request</TITLE>\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "    400 Bad Request\n" +
                "  </BODY>\n" +
                "</HTML>\n" +
                "\n";
        sendHeader(os, BAD_REQUEST, "text/html", response.getBytes().length);
        os.write((response + CRLF).getBytes());
        os.write(CRLF.getBytes());
        os.flush();
    }


    private void GETRequest(OutputStream os, String fileName) throws IOException{
        File f = new File(fileName);
        if(!f.exists()){
            fileNotFound(os);
        }else{
            FileInputStream fis = new FileInputStream(f);
            long fileSize = f.length();
            sendHeader(os, OK, contentType(fileName), fileSize);
            sendBytes(fis, os);
        }
    }

 
    private void fileNotFound(OutputStream os) throws IOException{
        String response = "<!DOCTYPE html>\n" +
                "<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Not Found</TITLE>\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "    404 Not Found\n" +
                "  </BODY>\n" +
                "</HTML>";
        sendHeader(os, FILE_NOT_FOUND, "text/html", response.getBytes().length);
        os.write((response + CRLF).getBytes());
        os.write(CRLF.getBytes());
        os.flush();
    }


    private void sendHeader(OutputStream os, String status, String contentType, long fileSize){
        try {
            os.write(("HTTP/1.1 " + status + CRLF).getBytes());             // Status line
            os.write(("Content-type: " + contentType + CRLF).getBytes());   // Content type line
            os.write(("Date: " + DATE + CRLF).getBytes());                  // Date line
            os.write(("Content-Length: " + fileSize + CRLF).getBytes());    // FileSize line
            os.write(CRLF.getBytes());                                      // Header have to end with CRLF
            os.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    private void sendBytes(FileInputStream fis, OutputStream os){
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        try{
            while ((bytes = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytes);
            }
            os.write(CRLF.getBytes());
            os.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

  
    private static String contentType(String fileName) {
        if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if(fileName.endsWith(".jpg")) {
            return "image/jpg";
        }
        return "application/octet-stream"; }


    @Override
    public void run() {
        try{
            processRequest();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
