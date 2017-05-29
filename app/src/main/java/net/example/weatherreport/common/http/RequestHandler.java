package net.example.weatherreport.common.http;

/**
 * Created by Carlo on 27/05/2017.
 */

public interface RequestHandler<T> {

    void requestCompleted(T data);

    void requestFailed(Throwable error);

}
