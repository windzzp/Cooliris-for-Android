package com.gtx.cooliris.entity;

import android.content.res.Resources;
import android.util.SparseArray;

import com.gtx.cooliris.R;
import com.gtx.cooliris.app.CoolirisApplication;

public class ImageGroupCategory {

    // All categories
	public static int CATEGORY_INVALID		= -1;
    public static int CATEGORY_ALL          = 0;
    public static int CATEGORY_ILLUSTRATION = 1;
    public static int CATEGORY_PHOTOGRAPHY  = 2;
    public static int CATEGORY_LIFE         = 3;
    public static int CATEGORY_INSPIRATION  = 4;
    //public static int CATEGORY_WEBDESIGN    = 5; // NOTE: It has been merged into CATEGORY_DESIGN
    public static int CATEGORY_DESIGN       = 5;
    public static int CATEGORY_DOWNLOAD     = 6;
    public static int CATEGORY_TUTORIALS    = 7;
    public static int CATEGORY_ORIGINAL     = 8;
    public static int CATEGORY_FAVOTITE     = 9;
    
    public static int CATEGORY_COUNT        = 10;

    // Category map
    private static SparseArray<String> s_categoryMap = new SparseArray<String>();
    static {
        Resources resources = CoolirisApplication.getAppContext().getResources();
        String[] categories = resources.getStringArray(R.array.category);

        if (categories.length == CATEGORY_COUNT) {
            for (int ix = CATEGORY_ALL; ix < CATEGORY_COUNT; ++ix) {
                s_categoryMap.put(ix, categories[ix]);
            }
        }
    };

    // TODO:
    // NOTE: For English version, it should return the zh-CN string to search
    public static String getCategoryName(int categoryType) {
        return s_categoryMap.get(categoryType);
    }

    public static int getCategory(String categoryName) {
        int size = s_categoryMap.size();
        for (int ix = 0; ix < size; ++ix) {
            if (categoryName.equals(s_categoryMap.valueAt(ix))) {
                return s_categoryMap.keyAt(ix);
            }
        }

        return CATEGORY_INVALID;
    }
}
