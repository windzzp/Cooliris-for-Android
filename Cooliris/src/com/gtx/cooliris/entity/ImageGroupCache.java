package com.gtx.cooliris.entity;

import java.util.ArrayList;

import android.util.SparseArray;

import com.gtx.cooliris.imagecache.ImageWorker.ImageWorkerAdapter;
import com.gtx.cooliris.utils.LogUtil;

public class ImageGroupCache {
    
    private static final String TAG = "ImageGroupCache";
    private static final int   DEFAULT_CAPACITY         = 100;
    private static final float DEFAULT_CAPACITY_PERCENT = 0.2f;
    private static final int   RANGE_UNDEFINED          = -1;

    /** Current category type, one of {@link #CATEGORY_ALL} */
    protected int mCategory;

    /** The cached group ids */
    protected int[] mCachedIds;

    /** Current range, start & end */
    protected int mStart = RANGE_UNDEFINED;
    protected int mEnd = RANGE_UNDEFINED;

    /** Current grouped data, range [start, end] */
    protected ArrayList<ImageGroup> mCurrentGroups;

    /** ImageGroup adapter */
    protected ImageGroupsAdapter mCacheGroupsAdapter = new ImageGroupsAdapter();

    /** Images adapter */
    protected ArrayList<Image> mCurrentImages = new ArrayList<Image>();

    /** Image list adapter */
    protected ImageListAdapter mCacheImagesAdapter = new ImageListAdapter();

    public ImageGroupCache(int category) {
        mCategory = category;
    }

    public int getCategory() {
        return mCategory;
    }

    public String getCategoryName() {
        return ImageGroupCategory.getCategoryName(mCategory);
    }

    public int[] getIds() {
        return mCachedIds;
    }

    public void setIds(int[] ids) {
        mCachedIds = ids;
    }

    public void setCurrentRange(int start, int end) {
        mStart = Math.max(0, start);
        mEnd = Math.max(0, end);

        if (null != mCachedIds) {
            mEnd = Math.min(mCachedIds.length, end);
        }
    }

    public int[] getCurrentRangeIds() {
        return copyIds(mStart, mEnd, mCachedIds);
    }

    public ArrayList<ImageGroup> getImageGroups() {
        return mCurrentGroups;
    }

    public ArrayList<Image> getImages() {
        return mCurrentImages;
    }

    public void setCurrentGroups(ArrayList<ImageGroup> imageGroups) {
        mCurrentGroups = imageGroups;
    }

    public void addGroups(ArrayList<ImageGroup> groups, boolean isOldData) {
        if (null == groups || 0 == groups.size()) {
            return;
        }

        if (null == mCurrentGroups) {
            int initSize = (null == mCachedIds) ?
                    DEFAULT_CAPACITY : (int)(mCachedIds.length * DEFAULT_CAPACITY_PERCENT);
            mCurrentGroups = new ArrayList<ImageGroup>(initSize);
        }

        // Add the new groups into the group list
        if (isOldData) {
            mCurrentGroups.addAll(groups);
            // mStart = ;
            // mEnd = ;
        } else {
            mCurrentGroups.addAll(0, groups);
            // mStart = ;
            // mEnd = ;
        }

        // Reset the Group Adapter data
        mCacheGroupsAdapter.setImageGroups(mCurrentGroups);

        // Add new images into the image list
        mCurrentImages.addAll(getAllImages(groups));

        // Reset the Image Adapter data
        mCacheImagesAdapter.setImages(mCurrentImages);
    }

    public int[] getMoreRange(int requestCount, boolean isOld) {
        if (null == mCachedIds) {
            LogUtil.e(TAG, "Category [" + getCategoryName() + "]" + "mCachedIds is null");
            return null;
        }

        int length = mCachedIds.length;

        int newStart = mStart;
        int newEnd = mEnd;

        if (isOld) {
            newStart = mEnd + 1;
            newEnd += requestCount;
        } else {
            newStart -= requestCount;
            newEnd = mStart - 1;
        }

        newStart = Math.max(0, newStart);
        newEnd = Math.min(length - 1, newEnd);

        // Reset the index
        mStart = newStart;
        mEnd = newEnd;

        return copyIds(newStart, newEnd, mCachedIds);
    }
    
    public ImageWorkerAdapter getCacheGroupsAdapter() {
		return mCacheGroupsAdapter;    	
	}
    
    public ImageWorkerAdapter getCacheImagesAdapter() {
    	return mCacheImagesAdapter;
    }

    public void recollectImages(SparseArray<ImageGroup> cache) {
        /*
        // Clear all
        mCurrentImages.clear();

        // Add new images into the image list
        mCurrentImages.addAll(getAllImages(mCurrentGroups));

        // Reset the Image Adapter data
        mCacheImagesAdapter.setImages(mCurrentImages);
        */
    }

    public boolean hasMoreData() {
        if (null == mCachedIds || null == mCurrentGroups) {
            LogUtil.e(TAG, "Data has not been initialize");
            return false;
        }

        if (null != mCachedIds && null != mCurrentGroups) {
            return mCachedIds.length > mCurrentGroups.size();
        }

        return false;
    }

    /*
     * public ArrayList<ImageGroup> loadMoreGroups(int requestCount, boolean needFillContent) { }
     */

    @Override
    public String toString() {
        return "ImageGroup Cache Info: Category [" + getCategoryName() + "]" + 
                ", Cached Ids size = " + ((null != mCachedIds) ? mCachedIds.length : -1) + 
                ", Current size = " + ((null != mCurrentGroups) ? mCurrentGroups.size() : -1);
    }

    protected int[] copyIds(int start, int end, int[] srcIds) {
        int size = Math.max(0, end - start + 1);
        if (srcIds != null && size > 0 && start >= 0 && end >= 0) {
            int[] ids = new int[size];

            System.arraycopy(srcIds, start, ids, 0, size);
            return ids;
        }

        return null;
    }

    protected ArrayList<Image> getAllImages(ArrayList<ImageGroup> groups) {
        ArrayList<Image> images = new ArrayList<Image>();

        /*if (null != groups) {

            // We need to filter the favorite category
            if (ImageGroupCategory.CATEGORY_FAVOTITE == mCategory) {
                ArrayList<Image> tempImages = null;
                for (ImageGroup imageGroup : groups) {
                    tempImages = imageGroup.getImageList();
                    for (Image image : tempImages) {
                        if (image.isFavorite()) {
                            images.add(image);
                        }
                    }
                }
            } else {
                for (ImageGroup imageGroup : groups) {
                    images.addAll(imageGroup.getImageList());
                }
            }
        }*/

        // Default is collect all image into list
        if (null != groups) {
            for (ImageGroup imageGroup : groups) {
                images.addAll(imageGroup.getImageList());
            }
        }

        return images;
    }

    /**
     * Adapter for image group list.
     */
    public class ImageGroupsAdapter extends ImageWorkerAdapter {
        private ArrayList<ImageGroup> mGroups;

        public void setImageGroups(ArrayList<ImageGroup> groups) {
            mGroups = groups;
        }

        @Override
        public Object getOriginalItem(int index) {
            if (null != mGroups || index >= 0) {
                int size = mGroups.size();
                return (index < size) ? mGroups.get(index) : null;
            }
            return null;
        }

        @Override
        public Object getItem(int num) {
            if (null != mGroups || num >= 0) {
                int size = mGroups.size();
                return (num < size) ? mGroups.get(num).getThumbUrl() : null;
            }
            return null;
        }

        @Override
        public int getSize() {
            return (null != mGroups) ? mGroups.size() : 0;
        }
    };

    /**
     * Image list adapter for a specified group.
     */
    public class ImageListAdapter extends ImageWorkerAdapter {
        private ArrayList<Image> mImages = null;

        public void setImages(ArrayList<Image> images) {
            mImages = images;
        }

        @Override
        public Object getOriginalItem(int index) {
            if (null != mImages || index >= 0) {
                int size = mImages.size();
                return (index < size) ? mImages.get(index) : null;
            }
            return null;
        }

        @Override
        public Object getItem(int num) {
            return (null != mImages) ? mImages.get(num).getImageUrl() : null;
        }

        @Override
        public int getSize() {
            return (null != mImages) ? mImages.size() : 0;
        }
    };
}