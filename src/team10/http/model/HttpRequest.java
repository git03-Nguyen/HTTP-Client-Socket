package team10.http.model;

public class HttpRequest {
    private final URL url;
    private final String request;
   
    public HttpRequest(URL url) {
        this.url = url;
        request = "GET " + url.getPath() + " HTTP/1.1\r\n"
                    + "Accept: */*\r\n"
                    + "Host: " + url.getHost() + "\r\n"
                    + "Connection: keep-alive\r\n\r\n";
    }

    public URL getUrl() {
        return url;
    }

    public String getRequest() {
        return request;
    }
}
