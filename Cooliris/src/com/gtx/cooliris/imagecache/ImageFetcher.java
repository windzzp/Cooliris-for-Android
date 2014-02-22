/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gtx.cooliris.imagecache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.gtx.cooliris.BuildConfig;
import com.gtx.cooliris.R;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.interfaces.IAsyncImageView;
import com.gtx.cooliris.utils.FileUtils;
import com.gtx.cooliris.utils.LogUtil;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String HTTP_CACHE_DIR = "http";
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private DiskLruCache mHttpDiskCache;
    private File mHttpCacheDir;
    private boolean mHttpDiskCacheStarting = true;
    private final Object mHttpDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    private static final int CONNECT_TIMEOUT = 3 * 1000;
    private static final int SOCKET_TIMEOUT = 5 * 1000;
    
    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
        mHttpCacheDir = ImageCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
    }

    @Override
    protected void initDiskCacheInternal() {
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }

    private void initHttpDiskCache() {
        if (!mHttpCacheDir.exists()) {
            mHttpCacheDir.mkdirs();
        }
        synchronized (mHttpDiskCacheLock) {
            if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
                try {
                    mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
                    if (BuildConfig.DEBUG) {
                        LogUtil.d(TAG, "HTTP cache initialized");
                    }
                } catch (IOException e) {
                    mHttpDiskCache = null;
                }
            }
            mHttpDiskCacheStarting = false;
            mHttpDiskCacheLock.notifyAll();
        }
    }

    @Override
    protected void clearCacheInternal() {
        super.clearCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null && !mHttpDiskCache.isClosed()) {
                try {
                    mHttpDiskCache.delete();
                    if (BuildConfig.DEBUG) {
                        LogUtil.d(TAG, "HTTP cache cleared");
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "clearCacheInternal - " + e);
                }
                mHttpDiskCache = null;
                mHttpDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    @Override
    protected void flushCacheInternal() {
        super.flushCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    mHttpDiskCache.flush();
                    if (BuildConfig.DEBUG) {
                        LogUtil.d(TAG, "HTTP cache flushed");
                    }
                } catch (IOException e) {
                	try {
						mHttpDiskCache.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                    LogUtil.e(TAG, "flush - " + e);
                }
            }
        }
    }

    @Override
    protected void closeCacheInternal() {
        super.closeCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    if (!mHttpDiskCache.isClosed()) {
                        mHttpDiskCache.close();
                        mHttpDiskCache = null;
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(TAG, "HTTP cache closed");
                        }
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "closeCacheInternal - " + e);
                }
            }
        }
    }

    /**
    * Simple network connection check.
    *
    * @param context
    */
    private void checkConnection(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, R.string.toast_no_network_connection_toast, Toast.LENGTH_LONG).show();
            LogUtil.e(TAG, "checkConnection - no connection found");
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        if (BuildConfig.DEBUG) {
            LogUtil.d(TAG, "processBitmap - " + data);
        }

        final String key = ImageCache.hashKeyForDisk(data);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot = null;
        synchronized (mHttpDiskCacheLock) {
            // Wait for disk cache to initialize
            while (mHttpDiskCacheStarting) {
                try {
                    mHttpDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }

            if (mHttpDiskCache != null) {
                try {
                    snapshot = mHttpDiskCache.get(key);
                    if (snapshot == null) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(TAG, "processBitmap, not found in http cache, downloading...");
                        }
                        DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
                        if (editor != null) {
                            if (downloadUrlToStream(data,
                                    editor.newOutputStream(DISK_CACHE_INDEX))) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        snapshot = mHttpDiskCache.get(key);
                    }
                    if (snapshot != null) {
                        fileInputStream =
                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileDescriptor = fileInputStream.getFD();
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } catch (IllegalStateException e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        Bitmap bitmap = null;
        if (fileDescriptor != null) {
            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth,
                    mImageHeight, getImageCache());
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
        
        if (null != snapshot) {
            snapshot.close();
        }
        return bitmap;
    }
    
    /**
     * Add by liulin.
     * We use the {@link #org.apache.http.client.methods.HttpGet} to download image from network,
     * which can makes the socket cancel immediately if needed.
     * 
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @param task The invoker.
     * 
     * @return The downloaded and resized bitmap.
     */
    // Add by liulin
    // We use the { org.apache.http.client.methods.HttpGet
    //
    private Bitmap processBitmap(String data, BitmapWorkerTask task) {
        if (BuildConfig.DEBUG) {
            LogUtil.d(TAG, "processBitmap - " + data);
        }

        final String key = ImageCache.hashKeyForDisk(data);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot;
        synchronized (mHttpDiskCacheLock) {
            // Wait for disk cache to initialize
            while (mHttpDiskCacheStarting) {
                try {
                    mHttpDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }

            if (mHttpDiskCache != null) {
                try {
                    snapshot = mHttpDiskCache.get(key);
                    if (snapshot == null) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(TAG, "processBitmap, not found in http cache, downloading...");
                        }
                        DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
                        if (editor != null) {
                        	OutputStream os = editor.newOutputStream(DISK_CACHE_INDEX);
                        	
                        	// Copy local image if the file existed.
                        	// Otherwise, download load it.
                        	if (copyLocalImageToStream(data, os, task)) {
                        		editor.commit();
							} else {
								if (downloadUrlToStream(data, os, task)) {
	                                editor.commit();
	                            } else {
	                                editor.abort();
	                            }
							}
                        }
                        snapshot = mHttpDiskCache.get(key);
                    }
                    if (snapshot != null) {
                        fileInputStream =
                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileDescriptor = fileInputStream.getFD();

                        // Add by liulin
                        //copyStreamToLocalImage(data, fileInputStream, task);
                        //fileInputStream.reset();
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "processBitmap - " + e);
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        Bitmap bitmap = null;
        if (fileDescriptor != null) {
            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, mImageWidth, mImageHeight, getImageCache());
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
        return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    /**
     * Add by liulin.
     */
    @Override
    protected Bitmap processBitmap(Object data, BitmapWorkerTask task)
    {
        return processBitmap(String.valueOf(data), task);
    }
	
    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Add by liulin
            // Set connecting timeout and socket timeout.
			urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(SOCKET_TIMEOUT);
            
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            LogUtil.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }

    public static boolean downloadUrlToStream(String urlString, OutputStream outputStream, BitmapWorkerTask task) {
	    /*if (BuildConfig.DEBUG) {
	        LogUtil.d(TAG, "downloadBitmap - downloading - " + urlString);
	    }*/
	    
	    LogUtil.d(TAG, "downloadBitmap - downloading - " + urlString);
	
	    disableConnectionReuseIfNecessary();
	    BufferedOutputStream out = null;
	    BufferedInputStream in = null;
	    
	    HttpGet httpGet = new HttpGet(urlString);
	    HttpClient client = new DefaultHttpClient();
	    HttpParams params = client.getParams();
	    HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
	    HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
	    
	    if (null != task)
	    {
	        task.setURLConnection(httpGet);
	    }
	    
	    try {
	        HttpResponse httpResponse = client.execute(httpGet);
	        if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
	        {
	            HttpEntity entity = httpResponse.getEntity();
	            
	            LogUtil.d(TAG, "Print Response");
	            long contentLength = entity.getContentLength();
	            LogUtil.d(TAG, "Get content length = " + contentLength);
	            
	            /*in = new BufferedInputStream(entity.getContent(), IO_BUFFER_SIZE);
	            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
	
	            int b;
	            while ((b = in.read()) != -1)
	            {
	                out.write(b);
	            }*/
	            
	            // Get buffer from network
	            in = new BufferedInputStream(entity.getContent(), IO_BUFFER_SIZE);
	            ByteArrayOutputStream bufos = new ByteArrayOutputStream();
	
	            int b;
	            while ((b = in.read()) != -1)
	            {
	            	bufos.write(b);
	            }
	            byte[] buf = bufos.toByteArray();
	            
	            // Create image file by the buffer
	            copyStreamToLocalImage(urlString, new ByteArrayInputStream(buf), task);
	            // Write the buffer into output stream.
	            outputStream.write(buf);
	            
	            bufos.close();
	            
	            return true;
	        }
	    } catch (final IOException e) {
	        LogUtil.e(TAG, "Error in downloadBitmap - " + e.getMessage() + ", url = " + urlString);
	        //e.printStackTrace();
	    } catch (Exception e) {
	    	LogUtil.e(TAG, "Error in downloadBitmap - " + e.getMessage());
		}
	    finally {
	        if (out != null) {
	            try {
	                out.close();
	            } catch (final IOException e) {
	                LogUtil.e(TAG, "Error in downloadBitmap - " + e);
	            }
	        }
	        
	        // Add by liulin
	        if (null != outputStream) {
				try {
					outputStream.close();
				} catch (Exception e) {
					LogUtil.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
	        
	        if (null != in) {
				try {
					in.close();
				} catch (Exception e) {
					LogUtil.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
	    }
	
	    return false;
	}
    
    public static boolean copyLocalImageToStream(String urlString, OutputStream outputStream, BitmapWorkerTask task)
    {
    	IAsyncImageView v = task.getImageView();
    	if (null != v)
		{
    		Object o = v.getDataSource();
			if (null != o && o instanceof Image)
			{
			    Image image = (Image)o;
				String downloadPath = null;
				
				if (urlString.equals(image.getImageUrl()))
				{
					downloadPath = image.getImageDownloadPath();
					LogUtil.i(TAG, "Image, download path = " + downloadPath);
				}
				else if (urlString.equals(image.getThumbUrl()))
				{
					downloadPath = image.getImageThumbDownloadPath();
					LogUtil.i(TAG, "Thumb Image, download path = " + downloadPath);
				}
				
				if (!TextUtils.isEmpty(downloadPath))
				{
					
					File imageFile = new File(downloadPath);
					if (null != imageFile && imageFile.exists())
					{
						LogUtil.i(TAG, "Hit the download path = " + downloadPath);
						
						FileInputStream is = null;
						try
						{
							LogUtil.i(TAG, "Begin to copy image from folder... ");
							
							is = new FileInputStream(imageFile);
							return FileUtils.copyStream(is, outputStream);
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
						finally
						{
							if (null != is)
							{
								try
								{
								    LogUtil.i(TAG, "Close file stream... ");
									is.close();
									is = null;
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
							
							if (null != outputStream)
							{
								try
								{
									outputStream.close();
									outputStream = null;
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
    	return false;
    }

    public static boolean copyStreamToLocalImage(String urlString, InputStream is, BitmapWorkerTask task)
    {
    	IAsyncImageView v = task.getImageView();
    	if (null != v)
		{
    		Object o = v.getDataSource();
			if (null != o && o instanceof Image)
			{
			    Image image = (Image)o;
				String downloadPath = null;
								
				if (urlString.equals(image.getImageUrl()))
				{
					downloadPath = image.getImageDownloadPath();
				}
				else if (urlString.equals(image.getThumbUrl()))
				{
					downloadPath = image.getImageThumbDownloadPath();
				}
				
				if (!TextUtils.isEmpty(downloadPath))
				{
					File imageFile = new File(downloadPath);
					if (null != imageFile && !imageFile.exists())
					{
						LogUtil.i(TAG, "File not existed, copy cache into SDCard, path = " + downloadPath);
						
						// If parent folder is not existed, create it.
						if (!imageFile.getParentFile().exists())
						{
							imageFile.getParentFile().mkdirs();
						}
						
						FileOutputStream os = null;
						try
						{
							LogUtil.i(TAG, "Begin to copy folder... ");
							
							os = new FileOutputStream(imageFile);
							return FileUtils.copyStream(is, os);
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
						finally
						{
							if (null != is)
							{
								try
								{
									is.close();
									is = null;
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
							
							if (null != os)
							{
								try
								{
									os.flush();
									os.close();
									os = null;
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
    	return false;
    }
    
	/**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
