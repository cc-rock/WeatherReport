package net.example.weatherreport.common.http;

import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Carlo on 29/05/2017.
 */

public class HttpWorkerTest {

    HttpWorker<Object> httpWorker;
    ExecutorService mockExecutorService;
    HttpURLConnection mockHttpUrlConnection;
    StreamDecoder<Object> mockStreamDecoder;
    Handler mockMainThreadHandler = mock(Handler.class);
    Object mockData;
    ArgumentCaptor<Callable<Object>> callableCaptor;
    ArgumentCaptor<Runnable> runnableCaptor;
    RequestHandler<Object> requestHandler;

    String testUrl1 = "http://test.url.one";
    String testUrl2 = "http://test.url.two";
    String testUrl3 = "http://test.url.three";
    String testUrl4 = "http://test.url.four";

    @Before
    public void setup() throws IOException {
        mockExecutorService = mock(ExecutorService.class);
        mockHttpUrlConnection = mock(HttpURLConnection.class);
        mockData = new Object();
        mockStreamDecoder = mock(StreamDecoder.class);
        when(mockStreamDecoder.decodeStream(any(InputStream.class))).thenReturn(mockData);
        URLConnectionFactory urlConnectionFactory = new URLConnectionFactory() {
            @Override
            public URLConnection createConnection(URL url) throws IOException {
                return mockHttpUrlConnection;
            }
        };
        when(mockHttpUrlConnection.getInputStream()).thenReturn(mock(InputStream.class));

        httpWorker = new HttpWorker<>(mockExecutorService, mockMainThreadHandler, urlConnectionFactory, mockStreamDecoder, 3);
        callableCaptor = ArgumentCaptor.forClass(Callable.class);
        runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        requestHandler = mock(RequestHandler.class);
    }

    @Test
    public void testDownloadNoCacheHit() throws Exception {
        httpWorker.doRequest(testUrl1, requestHandler);
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        assertEquals(1, httpWorker.pendingTasks.size());
        callableCaptor.getValue().call();
        verify(mockMainThreadHandler, times(1)).post(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(requestHandler, times(1)).requestCompleted(mockData);
    }

    @Test
    public void testDownloadWithCacheHit() throws Exception {
        httpWorker.doRequest(testUrl1, requestHandler);
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        assertEquals(1, httpWorker.pendingTasks.size());
        callableCaptor.getValue().call();
        verify(mockMainThreadHandler, times(1)).post(runnableCaptor.capture());
        runnableCaptor.getValue().run();


        assertEquals(1, httpWorker.cache.size());
        assertEquals(0, httpWorker.pendingTasks.size());

        verify(mockHttpUrlConnection, times(1)).disconnect();

        httpWorker.doRequest(testUrl1, requestHandler);
        // check that no other tasks are submitted to executor service
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        verify(mockMainThreadHandler, times(2)).post(runnableCaptor.capture());
        runnableCaptor.getAllValues().get(2).run();
        verify(requestHandler, times(2)).requestCompleted(mockData);

        assertEquals(1, httpWorker.cache.size());
        assertEquals(0, httpWorker.pendingTasks.size());
    }

    @Test
    public void requestingTheSameUrlBeforeCompletionDoesNotTriggerAnotherRequest() throws Exception {
        httpWorker.doRequest(testUrl1, requestHandler);
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        assertEquals(1, httpWorker.pendingTasks.size());

        httpWorker.doRequest(testUrl1, requestHandler);
        // verify no other submission to executor service, but multiple handlers in the pending task
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        assertEquals(1, httpWorker.pendingTasks.size());
        assertEquals(2, httpWorker.pendingTasks.get(testUrl1).requestHandlers.size());

        callableCaptor.getValue().call();
        verify(mockMainThreadHandler, times(1)).post(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(requestHandler, times(2)).requestCompleted(mockData);
    }

    @Test
    public void cacheSizeIsRespected() throws Exception {
        httpWorker.doRequest(testUrl1, requestHandler);
        verify(mockExecutorService, times(1)).submit(callableCaptor.capture());
        callableCaptor.getValue().call();
        verify(mockMainThreadHandler, times(1)).post(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        assertEquals(1, httpWorker.cache.size());

        httpWorker.doRequest(testUrl2, requestHandler);
        verify(mockExecutorService, times(2)).submit(callableCaptor.capture());
        callableCaptor.getAllValues().get(2).call();
        verify(mockMainThreadHandler, times(2)).post(runnableCaptor.capture());
        runnableCaptor.getAllValues().get(2).run();

        assertEquals(2, httpWorker.cache.size());

        httpWorker.doRequest(testUrl3, requestHandler);
        verify(mockExecutorService, times(3)).submit(callableCaptor.capture());
        callableCaptor.getAllValues().get(5).call();
        verify(mockMainThreadHandler, times(3)).post(runnableCaptor.capture());
        runnableCaptor.getAllValues().get(5).run();

        assertEquals(3, httpWorker.cache.size());

        httpWorker.doRequest(testUrl4, requestHandler);
        verify(mockExecutorService, times(4)).submit(callableCaptor.capture());
        callableCaptor.getAllValues().get(9).call();
        verify(mockMainThreadHandler, times(4)).post(runnableCaptor.capture());
        runnableCaptor.getAllValues().get(9).run();

        // loaded 4 different urls, but cache size is still 3
        assertEquals(3, httpWorker.cache.size());
    }
}
