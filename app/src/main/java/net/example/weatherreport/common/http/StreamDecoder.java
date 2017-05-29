package net.example.weatherreport.common.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Carlo on 27/05/2017.
 */

public interface StreamDecoder<T> {

    T decodeStream(InputStream is) throws IOException;

}
