package team10.http;

import java.util.logging.Level;
import java.util.logging.Logger;
import team10.http.control.GET;

public class Main {

    public static void main(String[] urls) {
        for (String url : urls) {
            Thread th = new Thread() {
                @Override 
                public void run() {
                    try {
                        GET get = new GET(url, null);
                    } catch (Exception ex) {
                        System.err.println("URL: " + url + " is invalid.");
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            th.start();  
        }
    }

}
