package team10.http.model;

public class URL {
    private final String protocol;
    private final String host;
    private final String path;
    private final String url;

    public URL(String url) throws Exception {
        this.url = url;
        
        if (url.contains("://")) {
            protocol = url.split("://", 2)[0];
            url = url.split("://", 2)[1];
        } else {
            protocol = "http";
        }
        
        if (url.contains("/")) {
            host = url.split("/", 2)[0];
            path = "/" + url.split("/", 2)[1];
        } else {
            host = url;
            path = "/";
        }
    }
    
    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }
    
    public String getFileName() {
        if (isRoot()) return "";
        if (path.endsWith("/")) {
            String tempStr = path.substring(0, path.length() - 1);
            return tempStr.substring(tempStr.lastIndexOf("/") + 1, tempStr.length());
        }
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }
    
    public String getParentFolder() {
        if (isRoot()) return "";
        String tempStr = path.substring(0, path.lastIndexOf("/"));
        return tempStr.substring(tempStr.lastIndexOf("/") + 1, tempStr.length());
    }
    
    public boolean isFolder() {
        return !(path.length() > 1 && path.endsWith("/"));
    }   
    
    public boolean isRoot() {
        return path.equals("/");
    }
}
