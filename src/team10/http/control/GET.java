package team10.http.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import team10.http.model.HttpRequest;
import team10.http.model.HttpResponse;
import team10.http.model.URL;

public class GET {
    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private final URL url;
    private HttpRequest request;
    private HttpResponse response;
    
    private boolean isItemInFolder;
    
    public GET(String strURL, Socket socket) throws Exception {
        this.url = new URL(strURL);
        this.socket = socket;
        this.isItemInFolder = (socket != null);
        
        // First time connecting to server
        if (!isItemInFolder) {
            try {
                setUpConnection();
                System.out.println("CONNECTED TO " + url.getHost());
            } catch (IOException ex) {
                System.err.println("CANNOT CONNECT TO " + url.getHost());
                return;
            }
        }
        //  n_th-time connecting -> connection is keep-alive -> files is in a folder
        else {
            in = new BufferedInputStream(this.socket.getInputStream());
            out = new BufferedOutputStream(this.socket.getOutputStream());
        }
        
        try {
            sendRequest();
            receiveResponse();
            
            // URL is an indexing page that have enabled indexing feature
            if (response.isIndexPage()) {
                createFolder();
                
                List<String> items = response.getIndexItem();
                if (items == null) return;
                
                System.out.println("  Downloading " + items.size() + " items from " + url.getUrl());
                for (String item: items) {
                    new GET(this.url.getUrl() + item, this.socket);
                }
            }
            // URL is a file
            else {
                saveToFile();
            }
        } catch (Exception ex) {
            System.err.println("Error downloading " + url.getUrl());
        }
        
        // Disconnect to server if it is not a file in a folder
        // When all thing has been DONE or the transfer has been INTERUPTED
        if (!isItemInFolder && !this.socket.isClosed()) {
            close();
            System.out.println("DISCONNECTED TO " + url.getHost());
        }
    }
    
    // Connect to server on the first time and keep-alive it until downloading complete
    private void setUpConnection() throws IOException {
        this.socket = new Socket(url.getHost(), 80);
        in = new BufferedInputStream(this.socket.getInputStream());
        out = new BufferedOutputStream(this.socket.getOutputStream());
    }
    
    // Form and send request
    private void sendRequest() throws IOException {
        request = new HttpRequest(url);
        out.write(request.getRequest().getBytes());
        out.flush();
    }

    // Start to receive HTTP response by raw data (byte)
    private void receiveResponse() throws Exception {
        response = new HttpResponse(this.url);
        receiveHeader();
        receiveData();
    }
    
    private void receiveHeader() throws Exception {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int byte_read;
        
        while ((byte_read = in.read()) != -1) {
            byteBuff.write(byte_read);
            if (byteBuff.toString("iso-8859-1").endsWith("\r\n\r\n")) {
                break;
            }
        }
        
        response.setHeader(byteBuff.toByteArray());
    }
    
    private void receiveData() throws IOException {
        ByteArrayOutputStream byteData = new ByteArrayOutputStream();
        byte[] buffer;
        
        // Content-Length: ...
        if (!response.isChunkedEncoded()) {
            buffer = in.readNBytes(response.getContentLength());
            byteData.writeBytes(buffer);
        } 
        // Transfer-Encoding: chunked
        else {
            ByteArrayOutputStream chunkLen = new ByteArrayOutputStream();
            do {
                chunkLen.reset();
                while (!(chunkLen.toString("iso-8859-1").endsWith("\r\n"))) {
                    chunkLen.write(in.read());
                }
                
                int len = Integer.parseInt(chunkLen.toString("iso-8859-1").substring(0, chunkLen.size() - 2), 16);
                buffer = in.readNBytes(len);
                byteData.writeBytes(buffer);
                
                in.readNBytes(2); //Skip \r\n
                
            } while (!(chunkLen.toString("iso-8859-1").equals("0\r\n")));
        }
        
        response.setData(byteData.toByteArray());
    }
    
    // Save files to computer
    private void saveToFile() throws FileNotFoundException, IOException {
        // If different from code 200 OK
        if (!(response.getTransferCode() == 200)) {
            System.err.println("    Cannot download " + url.getUrl() + " - Error Code: " + response.getTransferCode());
            return;
        }

        // Construct the file name and file directory
        String fileName = "\\" + url.getHost();
        if (url.isRoot() || url.getUrl().endsWith("/")) fileName = fileName + "_index.html";
        else if (isItemInFolder) fileName = fileName + "_" + url.getParentFolder() + "\\" + url.getFileName();
        else fileName = "\\" + url.getHost() + "_" + url.getFileName();
        fileName = System.getProperty("user.dir") + fileName;
        File file = new File(fileName);
        
        // Start to write byte-by-byte data to file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(response.getData());
            fos.flush();
            System.out.println(String.format("    Downloaded: %s | %d B",fileName, file.length()));
            fos.close();
        } catch (Exception e) {
            if (file.exists()) file.delete();
            throw e;
        }
    }
    
    // If URL points to a folder, especially an indexing directory page
    private void createFolder() {
        if (!(response.getTransferCode()== 200)) {
            System.err.println("    Cannot download " + url.getUrl()
                    + " - Error Code: " + response.getTransferCode());
            return;
        }
        
        // Construct folder name and the folder directory
        String folderName;
        folderName = System.getProperty("user.dir") + "\\" + url.getHost()
                    + "_" + url.getFileName();
        File dir = new File(folderName);
        
        // Delete old folder and create a new folder
        if (dir.exists()) {
            dir.delete();
        }
        dir.mkdirs();      
    }
    
    // After all transfers have been done, close the connection
    // If the connection is interupt, close the connection
    private void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

}


