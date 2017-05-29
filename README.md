Weather Report Example

* The app uses the Model View Presenter architecture (in a simple form)
* The *ForecastJsonParser*, *HttpWorker*, *WeatherAPiCaller* and *ImageLoader* classes exist only because of the explicit request not to use external libraries: normally I would have used Gson for JSon parsing, Retrofit + rxjava for api calls and Picasso or Glide for image loading.
* Normally I would have used Dagger for dependency injection, instead of keeping the main dependencies attached to the Application object.
* For time reasons, I have limited the unit tests to those for the HttpWorker classes
* For time reasons, I didn't write any Espresso test, but I would normally do.
* I made some simplifications in the HttpWorker class:
    * the cache size should be in bytes, and not in number of files cached (this way if you cache a few very big files on a low memory device you could cause an out of memory error)
    * the cache entries should be invalidated after some time, or by checking the file's last modification date through the http HEAD request
    * the buffer size of the http connection input stream has been left with the default value: maybe it could be optimised better.
* The app has a known issue: if you launch a search and put the app in background by pressing the home button while the search is in progress, and the request finished while the app is in background, the result will never be displayed, because the presenter only sends the results to the view if it has a view attached, otherwise it discards them. The presenter should save the results and send them to the view when it is attached again.
