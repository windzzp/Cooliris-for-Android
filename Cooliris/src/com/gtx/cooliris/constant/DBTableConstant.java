package com.gtx.cooliris.constant;

public class DBTableConstant {
    
    // Columns of girls DB's girls table  ------------------------
    public static final String TAB_IMAGEGROUP_ID             = "_id";
    public static final String TAB_IMAGEGROUP_IS_READED      = "is_readed";
    public static final String TAB_IMAGEGROUP_TAG            = "tag";
    public static final String TAB_IMAGEGROUP_HOME_PAGE      = "uri";
    public static final String TAB_IMAGEGROUP_IS_FAVORITE    = "is_favorite";
    public static final String TAB_IMAGEGROUP_TITLE          = "title";
    public static final String TAB_IMAGEGROUP_THUMB_URL      = "cover";
    public static final String TAB_IMAGEGROUP_CATEGORY       = "category";
    public static final String TAB_IMAGEGROUP_TIME           = "time";
    public static final String TAB_IMAGEGROUP_DESCRIPTION    = "description";
    public static final String TAB_IMAGEGROUP_THUMB_IMAGE_COUNT = "image_count"; // TBD
    
    public static final int TAB_IMAGEGROUP_GIRL_ID_IX        = 0;
    public static final int TAB_IMAGEGROUP_IS_READED_IX      = 1;
    public static final int TAB_IMAGEGROUP_TAG_IX            = 4;
    public static final int TAB_IMAGEGROUP_HOME_PAGE_IX      = 7;
    public static final int TAB_IMAGEGROUP_IS_FAVORITE_IX    = 8;
    public static final int TAB_IMAGEGROUP_TITLE_IX          = 9;
    public static final int TAB_IMAGEGROUP_THUMB_URL_IX      = 10;
    public static final int TAB_IMAGEGROUP_CATEGORY_IX       = 11;
    public static final int TAB_IMAGEGROUP_TIME_IX           = 12;
    public static final int TAB_IMAGEGROUP_DESCRIPTION_IX    = 13;
    public static final int TAB_IMAGEGROUP_THUMB_IMAGE_COUNT_IX = 10;   // TBD

    // Columns of girls DB's girl_images table  ------------------------
    public static final String TAB_IMAGE_ID                  = "_id";
    public static final String TAB_IMAGE_PARENT_ID           = "article";
    public static final String TAB_IMAGE_URL                 = "src";
    public static final String TAB_IMAGE_THUMB_URL           = "thumb_url";  // TBD, current is no use
    public static final String TAB_IMAGE_IS_FAVORITE         = "is_favorite";
    
    public static final int TAB_IMAGE_ID_IX                  = 0;
    public static final int TAB_IMAGE_PARENT_ID_IX           = 3;
    public static final int TAB_IMAGE_URL_IX                 = 10;
    public static final int TAB_IMAGE_THUMB_URL_IX           = 4;   // TBD, current is no use
    public static final int TAB_IMAGE_IS_FAVORITE_IX		 = 11;
    public static final int TAB_IMAGE_DOWNLOAD_PATH_IX 		 = 6;   // TBD, current is no use
    public static final int TAB_IMAGE_THUMB_DOWNLOAD_PATH_IX = 7;  // TBD, current is no use
    
    public static final String GIRLS_DB_INSERT_COLUMNS = 
            TAB_IMAGEGROUP_ID            + ", " + 
            TAB_IMAGEGROUP_HOME_PAGE     + ", " + 
            TAB_IMAGEGROUP_TITLE         + ", " + 
            TAB_IMAGEGROUP_CATEGORY      + ", " + 
            TAB_IMAGEGROUP_TIME          + ", " + 
            TAB_IMAGEGROUP_DESCRIPTION   + ", " + 
            TAB_IMAGEGROUP_THUMB_URL;
    
    public static final String GIRL_IMAGE_DB_INSERT_COLUMNS = 
            TAB_IMAGE_PARENT_ID   + ", " + 
            TAB_IMAGE_URL + ", " + 
            TAB_IMAGE_THUMB_URL;
}
