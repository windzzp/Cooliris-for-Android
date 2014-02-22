package com.gtx.cooliris.entity;

import java.util.ArrayList;

import android.util.SparseArray;

public class FavoriteGroupCache extends ImageGroupCache {

	// TODO: TBD
	public FavoriteGroupCache(int category) {
		super(category);
	}
	
    public void recollectImages(SparseArray<ImageGroup> cache) {
    	if (null != cache) {
			int size = cache.size();
			
			// Find the favorite
			ArrayList<ImageGroup> favorites = new ArrayList<ImageGroup>();
			ImageGroup group = null;
			for (int ix = 0; ix < size; ++ix) {
				group = cache.valueAt(ix);
				if (group.hasFavorite()) {
					favorites.add(group);
				}
			}
			
			size = favorites.size();
			if (size > 0) {
				int[] ids = new int[size];
				//ArrayList<Image> images = new ArrayList<Image>(size);
				
				for (int ix = 0; ix < size; ++ix) {
					ids[ix] = favorites.get(ix).getId();
				}
				
				// Reset data
				mCachedIds = ids;
				
				// 1. Reset group
				// 2. Add new favorites into the image list
				// 3. Reset the Group Adapter data
				mCurrentGroups.clear();
				mCurrentGroups.addAll(favorites);
				mCacheGroupsAdapter.setImageGroups(mCurrentGroups);
				
				// 1. Reset images
				// 2. Add new images into the image list
				// 3. Reset the Image Adapter data
		    	mCurrentImages.clear();
				mCurrentImages.addAll(getAllImages(mCurrentGroups));
				mCacheImagesAdapter.setImages(mCurrentImages);
			}
		}
	}
	
	@Override
	protected ArrayList<Image> getAllImages(ArrayList<ImageGroup> groups) {
    	ArrayList<Image> images = new ArrayList<Image>();
    	
    	if (null != groups) {
    		
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
			}
		}
    	
    	return images;
	}
}
