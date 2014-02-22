package com.gtx.cooliris.bl;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.gtx.cooliris.constant.TAGConstant;
import com.gtx.cooliris.entity.FavoriteGroupCache;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.ImageGroupCache;
import com.gtx.cooliris.entity.ImageGroupCategory;
import com.gtx.cooliris.imagecache.ImageWorker.ImageWorkerAdapter;
import com.gtx.cooliris.utils.LogUtil;

public class ImageDataProvider {
    // Default request count
    public static final int DEFAULT_REQUEST_COUNT = 30;
    private static final int MSG_FETCH_IMAGEGROUP_LIST_COMPLETE = 1;

    private IDataLoadListener mDataLoadListener = null;
    private boolean mIsLoadingData = false;

    // private LinkedHashMap<Integer, Girl> m_imageGroupMap = new LinkedHashMap<Integer, Girl>();

    // New Data
    // Save the category<--->Image Group List
    private LinkedHashMap<Integer, ImageGroupCache> mCategoryGroupCaches = new LinkedHashMap<Integer, ImageGroupCache>();

    // Cache all ImageGroup
    private SparseArray<ImageGroup> mGlobalImageGroupCache = new SparseArray<ImageGroup>();

    // TODO: TBD
    private SparseArray<Image> mGlobalImageCache = new SparseArray<Image>();

    private static ImageDataProvider s_instance = new ImageDataProvider();

    public interface IDataLoadListener {
        public void onLoadImageGroupsFinished(boolean hasMoreData);
    }

    public static ImageDataProvider getInstance() {
        return s_instance;
    }

    private ImageDataProvider() {
    }

    public void loadImageGroupsAsync(int category, int requestCount, boolean isRequestOldData,
            IDataLoadListener listener) {
        if (mIsLoadingData) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "Current is loading data, return!");
            return;
        }

        mDataLoadListener = listener;

        final int finalCategory = category;
        final int finalRequestCount = requestCount;
        final boolean finalIsRequestOldData = isRequestOldData;

        // TODO: We'll use AsyncTask to replace
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Copy db file for first if need.
                if (!ResourceManager.hasInitialized()) {
                    ResourceManager.initialize();
                }

                // Load data from database
                // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                boolean hasMoreData = loadMoreImageGroupsByCategory(finalCategory, finalRequestCount, finalIsRequestOldData);

                m_loadDataHandler.obtainMessage(MSG_FETCH_IMAGEGROUP_LIST_COMPLETE, hasMoreData ? 1 : 0, 0).sendToTarget();
            }
        }, "loadImageGroupsAsync").start();
    }

    public void loadImageGroupsByCategory(int category, boolean isRequestOldData) {
        loadMoreImageGroupsByCategory(category, DEFAULT_REQUEST_COUNT, isRequestOldData);
    }

    public boolean loadMoreImageGroupsByCategory(int category, int requestCount, boolean isRequestOldData) {
        long start = System.currentTimeMillis();

        boolean isFavorite = (ImageGroupCategory.CATEGORY_FAVOTITE == category);

        // Get the cache data, create it if doesn't find
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            // Create group cache
            cache = isFavorite ? new FavoriteGroupCache(category) : new ImageGroupCache(category);
            // Initialize Ids for this category
            cache.setIds(ImageDBLoader.loadGroupIds(category));
            // Add it into cache
            mCategoryGroupCaches.put(category, cache);
        }

        // Load data from database
        int[] ids = cache.getMoreRange(requestCount, isRequestOldData);
        if (null == ids || 0 == ids.length) {
            return false;
        }

        ArrayList<ImageGroup> groups = ImageDBLoader.loadImageGroupsByIds(ids, isFavorite, mGlobalImageGroupCache);
        // ArrayList<ImageGroup> groups = ImageDBLoader.loadImageGroupsByIds(ids,
        // m_globalImageGroupCache);

        // Add data to cache
        cache.addGroups(groups, isRequestOldData);

        Log.d(TAGConstant.TAG_DATA_PROVIDER, cache.toString());
        LogUtil.i(TAGConstant.TAG_DATA_PROVIDER, "Load data takes time: " + (System.currentTimeMillis() - start)
                + " ms");

        // Send load groups taking time
        /*
         * EasyTracker.getTracker().sendTiming(GAConstant.TIME_CATEGORY_PERFORMANCE,
         * (System.currentTimeMillis() - start), GAConstant.TIME_NAME_LOAD_ALL_GIRLS, null);
         */

        return true;
    }

    private Handler m_loadDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_FETCH_IMAGEGROUP_LIST_COMPLETE:
                mDataLoadListener.onLoadImageGroupsFinished((msg.arg1 == 1));
                break;

            default:
                break;
            }

            mDataLoadListener = null;
        }
    };

    public SparseArray<ImageGroup> getGlobalImageGroupCache() {
        return mGlobalImageGroupCache;
    }

    public ImageGroup getImageGroup(int groupId) {
        return mGlobalImageGroupCache.get(groupId);
    }

    public ArrayList<ImageGroup> getImageGroups(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "cache data is not created, has't group adapter!");
            return null;
        }

        return cache.getImageGroups();
    }

    public ImageWorkerAdapter getImageGroupsAdapter(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "cache data is not created, has't group adapter!");
            return null;
        }

        return cache.getCacheGroupsAdapter();
    }

    public ArrayList<Image> getImages(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "cache data is not created, has't group adapter!");
            return null;
        }

        return cache.getImages();
    }

    public ImageWorkerAdapter getImagesAdapter(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "cache data is not created, has't image adapter!");
            return null;
        }

        return cache.getCacheImagesAdapter();
    }

    public void recollectFavoriteImages() {
        ImageGroupCache cache = mCategoryGroupCaches.get(ImageGroupCategory.CATEGORY_FAVOTITE);
        if (null != cache) {
            cache.recollectImages(mGlobalImageGroupCache);
        }
    }

    public boolean hasInitialize(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        return (cache != null);
    }

    public boolean hasMoreData(int category) {
        ImageGroupCache cache = mCategoryGroupCaches.get(category);
        if (null == cache) {
            LogUtil.e(TAGConstant.TAG_DATA_PROVIDER, "cache data is not created, has't image adapter!");
            return true;
        }

        return cache.hasMoreData();
    }

    /**
     * Simple static empty adapter to use for images.
     */
    public final static ImageWorkerAdapter EMPTY_ADAPTER = new ImageWorkerAdapter() {
        private String[] m_emptyUrls = new String[0];

        @Override
        public Object getItem(int num) {
            return m_emptyUrls[0];
        }

        @Override
        public int getSize() {
            return 0;
        }
    };
}
