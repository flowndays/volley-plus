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

Todo:
1 Change class "ImageCache", to provide some methods to set items in cache to be permanent or not.
