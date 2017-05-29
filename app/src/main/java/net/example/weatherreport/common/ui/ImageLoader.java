package net.example.weatherreport.common.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.widget.ImageView;

import net.example.weatherreport.common.http.HttpWorker;
import net.example.weatherreport.common.http.RequestHandler;

import java.util.WeakHashMap;

/**
 * Created by Carlo on 29/05/2017.
 */

public class ImageLoader {

    private HttpWorker<Bitmap> httpWorker;
    private WeakHashMap<ImageView, String> pendingRequests;

    public ImageLoader(HttpWorker<Bitmap> httpWorker) {
        this.httpWorker = httpWorker;
        this.pendingRequests = new WeakHashMap<>();
    }

    public void load(final String url, final ImageView into) {
        // discard any other previous request on the same ImageView
        pendingRequests.put(into, url);

        httpWorker.doRequest(url, new RequestHandler<Bitmap>() {
            @Override
            public void requestCompleted(final Bitmap data) {
                String latestUrl = pendingRequests.get(into);
                // thanks to the WeakHashMap, if the ImageView has been garbage collected the entry won't be there anymore
                if (latestUrl != null && latestUrl.equals(url)) {
                    into.setImageDrawable(new BitmapDrawable(into.getResources(), data));
                }
            }

            @Override
            public void requestFailed(Throwable error) {
                // do nothing
            }
        });
    }

    public void cancelAllRequests() {
        httpWorker.cancelAllPendingTasks();
    }
}
