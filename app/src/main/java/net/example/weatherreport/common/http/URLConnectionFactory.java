package net.example.weatherreport.common.http;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Carlo on 27/05/2017.
 */

public class URLConnectionFactory {

    public URLConnection createConnection(URL url) throws IOException {
        return url.openConnection();
    }

}
