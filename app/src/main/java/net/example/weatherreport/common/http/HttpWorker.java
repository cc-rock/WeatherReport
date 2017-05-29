package net.example.weatherreport.common.http;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Created by carlo.conserva on 24/05/2017.
 */

public class HttpWorker<T> {

    private ExecutorService executorService;
    private Handler mainThreadHandler;
    private URLConnectionFactory urlConnectionFactory;
    private StreamDecoder<T> streamDecoder;

    private int cacheSize;
    final LinkedHashMap<String,T> cache;

    final HashMap<String, PendingTask> pendingTasks;

    public HttpWorker(ExecutorService executorService, Handler mainThreadHandler,
                      URLConnectionFactory urlConnectionFactory, StreamDecoder<T> streamDecoder, int cacheSize) {
        this.executorService = executorService;
        this.mainThreadHandler = mainThreadHandler;
        this.urlConnectionFactory = urlConnectionFactory;
        this.streamDecoder = streamDecoder;
        this.cacheSize = cacheSize;
        this.cache = new LinkedHashMap<>(cacheSize + 1);
        this.pendingTasks = new HashMap<>();
    }

    public void doRequest(final String url, final RequestHandler<T> handler) {
        synchronized (cache) {
            final T cached = cache.get(url);
            if (cached != null) {
                Log.d("HttpWorker", "CACHE HIT: " + url);
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handler.requestCompleted(cached);
                    }
                });
                return;
            }
        }
        synchronized (pendingTasks) {
            final PendingTask pendingTask = pendingTasks.get(url);
            if (pendingTask != null) {
                pendingTask.requestHandlers.add(handler);
                Log.d("HttpWorker", "Loading already in progress: " + url);
                return;
            }
            PendingTask newPendingTask = new PendingTask();
            newPendingTask.requestHandlers.add(handler);
            pendingTasks.put(url, newPendingTask);
            executorService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    try {
                        final T data = downloadData(url);
                        Log.d("HttpWorker", "Download successful: " + url);
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                requestCompletedForUrl(url, data);
                            }
                        });
                        return data;
                    } catch (final Exception e) {
                        Log.d("HttpWorker", "Exception", e);
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                requestFailedForUrl(url, e);
                            }
                        });
                        throw(e);
                    }
                }
            });
        }
    }

    public void cancelAllPendingTasks() {
        // the pending downloads will finish anyway and get cached,
        // but the handlers are cleared and won't be called
        pendingTasks.clear();
    }

    private void requestCompletedForUrl(String url, T data) {
        synchronized (cache) {
            cache.put(url, data);
            if (cache.size() > cacheSize) {
                cache.remove(cache.keySet().iterator().next());
            }
        }
        synchronized (pendingTasks) {
            PendingTask pendingTask = pendingTasks.get(url);
            if (pendingTask != null) {
                for(RequestHandler<T> requestHandler : pendingTask.requestHandlers) {
                    requestHandler.requestCompleted(data);
                }
                pendingTasks.remove(url);
            }
        }
    }

    private void requestFailedForUrl(String url, Throwable error) {
        synchronized (pendingTasks) {
            PendingTask pendingTask = pendingTasks.get(url);
            if (pendingTask != null) {
                for(RequestHandler<T> requestHandler : pendingTask.requestHandlers) {
                    requestHandler.requestFailed(error);
                }
                pendingTasks.remove(url);
            }
        }
    }

    private T downloadData(String url) throws IOException {
        URLConnection connection = urlConnectionFactory.createConnection(new URL(url));
        connection.setConnectTimeout(10000); // 10 seconds
        connection.setReadTimeout(10000); // 10 seconds
        try {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            return streamDecoder.decodeStream(in);
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection)connection).disconnect();
            }
        }
    }

    class PendingTask {
        List<RequestHandler<T>> requestHandlers = new ArrayList<>();
    }
}
