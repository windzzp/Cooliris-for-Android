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

import java.lang.ref.WeakReference;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.ImageView;

import com.gtx.cooliris.BuildConfig;
import com.gtx.cooliris.db.ImagesTableBL;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.interfaces.IAsyncImageView;
import com.gtx.cooliris.utils.LogUtil;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    protected Context mContext;
    protected ImageWorkerAdapter mImageWorkerAdapter;

    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(Object data, IAsyncImageView imageView) {
        if (data == null) {
            return;
        }

        BitmapDrawable value = null;

        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (value != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
        }
    }

    /**
     * Load an image specified from a set adapter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap. {@link ImageWorker#setAdapter(ImageWorkerAdapter)} must be called before using this
     * method.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(int num, IAsyncImageView imageView) {
        if (mImageWorkerAdapter != null) {
        	// Add by liulin
        	// Set the image view's data, such as {@link Girl} or {@link GirlImage}
        	//imageView.setDataSource(mImageWorkerAdapter.getOriginalItem(num));
        	
            loadImage(mImageWorkerAdapter.getItem(num), imageView);
        } else {
            throw new NullPointerException("Data not set, must call setAdapter() first.");
        }
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(FragmentManager fragmentManager,
            ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param activity
     * @param diskCacheDirectoryName See
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(Context, String)}.
     */
    public void addImageCache(FragmentActivity activity, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
        mImageCache = ImageCache.getInstance(activity.getSupportFragmentManager(), mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }
    
    protected String prepareUrl(Object data, BitmapWorkerTask task)
    {
        String url = String.valueOf(data);
        if (TextUtils.isEmpty(url))
        {
            IAsyncImageView imageView = task.getAttachedImageView();
            if (null != imageView)
            {
                Object object = imageView.getDataSource();
                if (object instanceof Image)
                {
                    Image image = (Image)object;
                    ImageGroup girl = image.getParent();
                    if (null != girl && !girl.hasInited())
                    {
                        LogUtil.e(TAG, "Load image*********Start*********");
                        ImagesTableBL.getInstance().fillGroupImages(girl);
                        LogUtil.e(TAG, "Load image*********End*********");
                    }
                    else 
                    {
                        if (null == girl)
                        {
                            LogUtil.e(TAG, "Error: Parent error!");
                        }
                    }
                                        
                    //url = imageView.isDisplayThumb() ? image.getThumbUrl() : image.getImageUrl();
                    url = image.getImageUrl();
                    LogUtil.e(TAG, "Is thumb ? " + imageView.isDisplayThumb() + ", Getted URL: " + url);
                    
                    if (TextUtils.isEmpty(url))
                    {
                        LogUtil.e(TAG, "Error: URL is empty!!!");
                    }
                }
                else 
                {
                    LogUtil.e(TAG, "Error: Data source is Not correct");
                }
            }
        }
        
        return url;
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(Object data);
    
    /**
     * Add by liulin.
     */
    protected abstract Bitmap processBitmap(Object data, BitmapWorkerTask task);
    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(IAsyncImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            
            // Add by liulin
            // Cancel the HTTP connection
            //bitmapWorkerTask.cancelConnection();
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.data;
                LogUtil.e(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, IAsyncImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                
                // Add by liulin
                // Cancel the HTTP connection
                //bitmapWorkerTask.cancelConnection();

                if (BuildConfig.DEBUG) {
                    LogUtil.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(IAsyncImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    public class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
        private Object data;
        private final WeakReference<IAsyncImageView> imageViewReference;
        private HttpGet m_urlConnection = null;

        public BitmapWorkerTask(IAsyncImageView imageView) {
            imageViewReference = new WeakReference<IAsyncImageView>(imageView);
        }
        
        public HttpGet getHttpConnection()
        {
            return m_urlConnection;
        }
        
        public void setURLConnection(HttpGet urlConnection)
        {
            m_urlConnection = urlConnection;
        }
        
        public boolean cancelConnection()
        {
            if (null != m_urlConnection)
            {
                try
                {
                    LogUtil.i(TAG, "abort connection! " + m_urlConnection.getURI());
                    m_urlConnection.abort();
                    m_urlConnection = null;
                }
                catch (Exception e)
                {
                	LogUtil.e(TAG, "Cancel Connection: " + data);
                }
                
                return true;
            }
            else 
            {
                LogUtil.i(TAG, "Nothing connection to be abort!");
                return false;
            }
        }
        
        public IAsyncImageView getImageView()
		{
			if (null != imageViewReference)
			{
				return imageViewReference.get();
			}
			
			return null;
		}

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Object... params) {
            if (BuildConfig.DEBUG) {
                LogUtil.d(TAG, "doInBackground - starting work");
            }
            
            data = prepareUrl(params[0], this);
            String url = String.valueOf(data);
            
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        LogUtil.e(TAG, "Wait.(0).................");
                        mPauseWorkLock.wait();
                        LogUtil.e(TAG, "Wait.(1).................");
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(url);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                // Modified by liulin
            	//bitmap = processBitmap(params[0]);
                //bitmap = processBitmap(params[0], this);
                //bitmap = processBitmap(url, this);
                bitmap = processBitmap(url);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                if (Utils.hasHoneycomb()) {
                    // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                    drawable = new BitmapDrawable(mResources, bitmap);
                } else {
                    // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                    // which will recycle automagically
                    drawable = new RecyclingBitmapDrawable(mResources, bitmap);
                }

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(url, drawable);
                }
            }

            if (BuildConfig.DEBUG) {
                LogUtil.d(TAG, "doInBackground - finished work");
            }

            return drawable;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final IAsyncImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    LogUtil.d(TAG, "onPostExecute - setting bitmap");
                }
                setImageDrawable(imageView, value);
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private IAsyncImageView getAttachedImageView() {
            final IAsyncImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);

            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be 
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(IAsyncImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            
            // Set background to loading bitmap
            if (null != mLoadingBitmap) {
                imageView.setBackgroundDrawable(
                        new BitmapDrawable(mResources, mLoadingBitmap));   
            } 
            
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
	
    /**
     * Set the simple adapter which holds the backing data.
     *
     * @param adapter
     */
    public void setAdapter(ImageWorkerAdapter adapter) {
        mImageWorkerAdapter = adapter;
    }

    /**
     * Get the current adapter.
     *
     * @return
     */
    public ImageWorkerAdapter getAdapter() {
        return mImageWorkerAdapter;
    }

    /**
     * A very simple adapter for use with ImageWorker class and subclasses.
     */
    public static abstract class ImageWorkerAdapter {
    	// Add by liulin
    	public Object getOriginalItem(int index) { return null; }
    	
        public abstract Object getItem(int num);
        public abstract int getSize();
        
        //public abstract void reset();
    }
}
