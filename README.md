volley-plus
===========

My extend of volley. 

The main purpose is to add a image disk cache to the ImageLoader model. Moreover, one can control the 2 level cache better.

I'll add some functions to keep some items in the cache able to be persistent, to fit cases when reusable images needed.

In my app, a book reader, the books' covers need to be saved or cached in sd card. The covers should be kept untill the book is deleted.
This is why I decided alter the volley project.

Changes from trunk of volley:

1 Cache interface of ImageLoader is changed, a mixed image cache goes instead of the original one. 

2 Package "com.android.volley.plus" contains additional codes.

3 TransitionImageListener provides a more comfortable transition animation than the original one, which shows the image directly.
  To sustain memory, TransitionImageListeners are kept in a pool.

4 Change class "ImageCache", to provide some methods to set items in cache to be persistent or brittle(normal state).

5 Provide a more developer-friendly RequestPro base class to extend. RequestPro's doc:
 * Extended from the default Request provided by volley source with difference as follows:
 * 1    Default successListener.
 * 2    Unified interface for get or post request. Both can add params as a constructor param. As for get, extra params will be appended to the original url.
 * 3    Params is also crucial when generating a cache key, to evade improper cache when requests differ only from params.
 * 4    The method getEncodedParameters can be used by sub classes.
see Example to use RequestPro for detail.

6 Record image download failers to avoid unnecessory redownloads.

7 Change class BasicNetwork, to support getting streams from sd card, not only from network. Use format:"file://sdcard..." to get them.


Example to use ImageLoader:

        mQueue = Volley.newRequestQueue(context);
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(context, FileManager.CACHE_IMAGE_PATH_NEW);
        cacheParams.setMemCacheSizePercent(context, 0.2f);
        imageCache = new ImageCache(cacheParams);
        mImageLoader = new ImageLoader(mQueue, imageCache);
        ......
        String url = "http://www.google.com/...";
        ImageView imageView = ...;
        mImageLoader.get(url, ImageWorkerManager.getDefaultListener(imageView), width, height);
       
       //Example to use RequestPro:
       
       HashMap<String, String> params = new HashMap<String, String>();
        params.put(DownloadParams.RANK_ID, Integer.toString(type));
        params.put(DownloadParams.BOOK_SEARCH_PAGE, Integer.toString(page));
        params.put(DownloadParams.BOOK_SEARCH_COUNT, Integer.toString(Constants.PAGE_SIZE));
        params.put(DownloadParams.UUID_PARAM_ANDROIDID, Constants.getUuid());
        BookListRequest request = new BookListRequest(Request.Method.POST, DownloadParams.BOOK_LIST_URL, params, responseListener, errorListener);
        mQueue.add(request);
        mQueue.start();
        
       // BookListRequest Class:
        
        public class BookListRequest extends RequestPro<Result<ArrayList<NetBook>>> {
        
        public BookListRequest(int method, String url, Map<String,String> params, Response.Listener<Result<ArrayList<NetBook>>> listener, Response.ErrorListener errorListener) {
          super(method, url, params, listener, errorListener);
        }
        
      @Override
       protected VolleyError parseNetworkError(VolleyError volleyError) {
         if (volleyError.getMessage().equals("empty"))
             return new EmptyError();
         return super.parseNetworkError(volleyError);
      }
      @Override
      protected Response<Result<ArrayList<NetBook>>> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Result<ArrayList<NetBook>> result = GsonUtil.fromJson(jsonString, new TypeToken<Result<ArrayList<NetBook>>>() {
            }.getType());
            if (result != null && result.getContent() != null) {
                if (!result.getContent().isEmpty()) {
                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return Response.error(parseNetworkError(new VolleyError("empty")));
                }
            } else return Response.error(parseNetworkError(new VolleyError("error")));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
      }
    }
