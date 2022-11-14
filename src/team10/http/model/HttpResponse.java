package team10.http.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponse {
    private byte[] header;
    private byte[] data;
    private final URL url;

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public HttpResponse(URL url) {
        this.url = url;
    }
    
    public boolean isChunkedEncoded() {
        return (new String(header)).contains("Transfer-Encoding: chunked");
    }
    
    // Content length equals to Content-Length: <value>
    // or the length of data after completely receiving data (chunked)
    public int getContentLength() {
        if (isChunkedEncoded()) {
            if (data == null) 
                return 0;
            return data.length;
        }
        
        String strHeader = new String(header);
        int start = strHeader.indexOf("Content-Length:") + 16;
        int end = start;
        while (strHeader.charAt(end) != '\r' && strHeader.charAt(end) != '\n') end++;
        return Integer.parseInt(strHeader.substring(start, end));
    }
    
    public int getTransferCode() {
        String strHeader = new String(header);
        return Integer.parseInt(strHeader.substring(strHeader.indexOf(" ") + 1,strHeader.indexOf(" ") + 4));
    }
    
    // Parse HTML containing "<title> Index of <path> </title>" using REGEX
    // Check if it is truly an indexing webpage
    public boolean isIndexPage() {
        String html = new String(data);
        html = html.replaceAll("\n", " ").replaceAll("\r", " ");
        html = html.replaceAll("\\s+", " ");
        
        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(html);
        
        return (m.find() 
                && m.group(1).endsWith("Index of " + url.getPath().substring(0, url.getPath().length() - 1)));
    }
    
    // Parse HTML containing "<a href=<link> .. >" to find all links in the indexing webpage using REGEX
    // Exclude queries and links to parent directories
    // Put them all to a list of String to download later (
    public List<String> getIndexItem() {
        if (!isIndexPage()) return null;
        List<String> items = new ArrayList<>();
        
        String html = new String(data);
        html = html.replaceAll("\n", " ").replaceAll("\r", " ");
        html = html.replaceAll("\\s+", " ");
        
        Pattern p = Pattern.compile("href=\"(.*?)\"");
        Matcher m = p.matcher(html);
        while (m.find()) {
            String item = m.group(1);
            if (!url.getPath().contains(item) && !item.startsWith("?")) {
                items.add(item);
            }
        }
        
        return items;
    }
}

