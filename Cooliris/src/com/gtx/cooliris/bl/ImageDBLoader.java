package com.gtx.cooliris.bl;

import java.util.ArrayList;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.gtx.cooliris.constant.TAGConstant;
import com.gtx.cooliris.db.ImageGroupTableBL;
import com.gtx.cooliris.db.ImagesTableBL;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.ImageGroupCategory;
import com.gtx.cooliris.utils.LogUtil;

public class ImageDBLoader
{
	private static final String TAG = "ImageDBLoader";
	
    public static ArrayList<ImageGroup> loadAllGirls()
    {
        ArrayList<ImageGroup> girls = ImageGroupTableBL.getInstance().loadAllGroups();
        
        long start = System.currentTimeMillis();
        for (ImageGroup girl : girls)
        {
            ImagesTableBL.getInstance().loadGroupImages(girl);
            //LogUtil.d(TAGConstant.TAG_LOAD_DATA_FROM_DB, 
            //        "girl id = " + girl.getGirlId() + ", image size = " + images.size());
        }
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Load images takes time = " + (System.currentTimeMillis() - start));
        
        return girls;
    }
    
    public static ArrayList<ImageGroup> loadOnlyGirls()
    {
        long start = System.currentTimeMillis();
        ArrayList<ImageGroup> girls = ImageGroupTableBL.getInstance().loadAllGroups();
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Load images takes time = " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        //ArrayList<GirlImage> images = null;
        //GirlImage image = null;
        int count = 0;
        for (ImageGroup girl : girls)
        {
            //images = new ArrayList<GirlImage>(girl.getImageCount());
            //girl.setImageList(images);
            
            count = girl.getImageCount();
            for (int ix = 0; ix < count; ix++)
            {
                girl.addImage(new Image());
            }
        }
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Create empty images takes time = " + (System.currentTimeMillis() - start));
        
        return girls;
    }
    
    public static int[] loadGroupIds(int category)
    {
		if (ImageGroupCategory.CATEGORY_FAVOTITE == category) {
			return ImagesTableBL.getInstance().getFavoriteImageGroupIds2();
		} else {
			return ImageGroupTableBL.getInstance().loadGroupIds(category);
		}
    }
    
    public static ArrayList<ImageGroup> loadImageGroups()
    {
        long start = System.currentTimeMillis();
        ArrayList<ImageGroup> imageGroups = ImageGroupTableBL.getInstance().loadAllGroups();
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Load images takes time = " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        //ArrayList<GirlImage> images = null;
        //GirlImage image = null;
        int count = 0;
        for (ImageGroup group : imageGroups)
        {
//            images = new ArrayList<GirlImage>(girl.getImageCount());
//            girl.setImageList(images);
//            
//            count = group.getImageCount();
//            for (int ix = 0; ix < count; ix++)
//            {
//                group.addImage(new GirlImage());
//            }
            
            ImagesTableBL.getInstance().loadGroupImages(group);
        }
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Create empty images takes time = " + (System.currentTimeMillis() - start));
        
        return imageGroups;
    }
    
    public static ArrayList<ImageGroup> loadImageGroupsByCategory(String category)
    {
        long start = System.currentTimeMillis();
        ArrayList<ImageGroup> girls = ImageGroupTableBL.getInstance().loadAllGroups();
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Load images takes time = " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        //ArrayList<GirlImage> images = null;
        //GirlImage image = null;
        int count = 0;
        for (ImageGroup girl : girls)
        {
            //images = new ArrayList<GirlImage>(girl.getImageCount());
            //girl.setImageList(images);
            
            count = girl.getImageCount();
            for (int ix = 0; ix < count; ix++)
            {
                girl.addImage(new Image());
            }
        }
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Create empty images takes time = " + (System.currentTimeMillis() - start));
        
        return girls;
    }
    
    // TODO: TBDelete
    
    public static ArrayList<ImageGroup> loadImageGroupsByIds(ArrayList<Integer> ids)
    {
        long start = System.currentTimeMillis();
        
        // 1. Load image groups by ids
        // NOTE: It only load the group info, not contain the image list
        ArrayList<ImageGroup> imageGroups = ImageGroupTableBL.getInstance().loadImageGroupsByIds(ids);
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Load images takes time = " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        // 2. Fill group data
//        for (ImageGroup group : imageGroups)
//        {           
//            ImagesTableBL.getInstance().loadGroupImages(group);
//        }
        
        // We use SparseIntArray for better performance
        //HashMap<Integer, Integer> countMap = ImagesTableBL.getInstance().getImageCount(ids);
        SparseIntArray countMap = ImagesTableBL.getInstance().getImageCount(ids);
        
        int count = 0;
        for (ImageGroup group : imageGroups)
        {
            //count = ImagesTableBL.getInstance().getImageCount(group.getId());
            count = countMap.get(group.getId());
            for (int ix = 0; ix < count; ix++)
            {
                group.addImage(new Image());
            }
        }
        
        LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                "Create empty images takes time = " + (System.currentTimeMillis() - start));
        
        return imageGroups;
    }
    
    public static ArrayList<ImageGroup> loadImageGroupsByIds(int[] ids, SparseArray<ImageGroup> globalCache)
    {
    	if (null == ids || 0 == ids.length) {
			return new ArrayList<ImageGroup>();
		}
    	
        long start = System.currentTimeMillis();

        int size = ids.length;
        ArrayList<ImageGroup> resultGroups = new ArrayList<ImageGroup>(size);
        ArrayList<Integer> filteredIds = new ArrayList<Integer>(size);
        
        int ix = 0;
        for (int id : ids) {
        	
        	// If the id is not existed in cache, add it to query id list.
			if (null == globalCache.get(id)) {
				filteredIds.add(id);
			}
			
			ix++;
		}

        // 1. Load image groups by ids
        // NOTE: It only load the group info, not contain the image list
        SparseArray<ImageGroup> imageGroups = ImageGroupTableBL.getInstance().loadImageGroupsByIds2(filteredIds);
        
        // 2. Get the image count for different ids
        SparseIntArray countMap = ImagesTableBL.getInstance().getImageCount(filteredIds);
        
        // 3. Fill result
        for (int id : ids) {
        	// Check it has been insert into list for first, 
        	// if not found, get it from the list which we got from DB.
        	ImageGroup cacheGroup = globalCache.get(id);
        	if (null != cacheGroup) {
        		// Add it into result group
    			resultGroups.add(cacheGroup);
			} else {
				ImageGroup tempGroup = imageGroups.get(id);
				int count = countMap.get(id);

				if (null != tempGroup) {
					for (int index = 0; index < count; index++) {
						tempGroup.addImage(new Image());
					}

					// Add it into result group
					resultGroups.add(tempGroup);

					/**
					 *  Important: add it into global cache
					 */
					globalCache.put(id, tempGroup);
				} else {
					LogUtil.e(TAG, "Load data from DB Error! ");
				}
			}
		}
        
        LogUtil.e(TAGConstant.TAG_PERFORMANCE, 
                "load Image Groups By Ids takes time = " + (System.currentTimeMillis() - start));
        
        return resultGroups;
    }
    
    public static ArrayList<ImageGroup> loadImageGroupsByIds(
    		int[] ids, boolean shouldLoadGroupImage, SparseArray<ImageGroup> globalCache)
    {
        ArrayList<ImageGroup> groups = loadImageGroupsByIds(ids, globalCache);
        
        if (shouldLoadGroupImage) {
			
        	long start = System.currentTimeMillis();
        	// Important:
        	// TODO: It's Poor Performing, we can use SQL to accelerate
        	for (ImageGroup imageGroup : groups) {
        		if (!imageGroup.hasInited()) {
        			ImagesTableBL.getInstance().fillGroupImages(imageGroup);
        		}
        	}
        	
        	/*
	        ArrayList<ImageGroup> notInitedGroups = new ArrayList<ImageGroup>();
	        for (ImageGroup imageGroup : groups) {
				if (!imageGroup.hasInited()) {
					notInitedGroups.add(imageGroup);
				}
			}
	        
	        // Fill the data which not fill images
	        ImagesTableBL.getInstance().fillGroupsImages(notInitedGroups);
        	 */
        	
        	LogUtil.e(TAGConstant.TAG_PERFORMANCE, 
        			"load Images of Groups takes time = " + (System.currentTimeMillis() - start));
		}
        return groups;
    }
}
