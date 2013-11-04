volley-plus
===========

My extend of volley. 

Cache interface of ImageLoader is changed, a mixed image cache goes instead of the original one. 

I add a package "com.android.volley.plus" to the trunk. And, make some changes in class com.android.volley.toolbox.ImageLoader.

The main purpose is to add a image disk cache to the ImageLoader model. Moreover, one can control the 2 level cache better.

I'll add some functions to keep some items in the cache able to be persistent, to fit cases when reusable images needed.

In my app, a book reader, the books' covers need to be saved or cached in sd card. The covers should be kept untill the book is deleted.
This is why I decided alter the volley project.
