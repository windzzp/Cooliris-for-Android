package com.gtx.cooliris.entity;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;
import android.text.TextUtils;

public class ImageGroup {
    private static final String DOWNLOAD_DIR_NAME       = "Cooliris";
    private static final String DOWNLOAD_THUMB_DIR_NAME = ".thumbs";
    private static final String DOWNLOAD_FILE_SUFFIX    = ".jpg";

    private static final File DOWNLOAD_ROOT_DIR = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DOWNLOAD_DIR_NAME);

    private int     m_groupId       = 0;    // ok
    private String  m_homePage      = "";   // ok
    private String  m_title         = "";   // ok
    private String  m_description   = "";   
    private String  m_date			= "";   // ok
    private String  m_thumbUrl      = "";   // ok
    
    // New
    private boolean m_isReaded      = false;
    private String  m_tag           = "";
    private String  m_category      = "";
    //private boolean m_isFavorite	= false;
    private boolean m_hasFavorite	= false;

    // TODO: Other, will be modified
    private int     m_imageCount    = 0;
    private boolean m_hasInited     = false;
    private String	m_downloadDir      = "";
    private String	m_downloadThumbDir = "";

    private ArrayList<Image> m_imageList = new ArrayList<Image>();

    public boolean isValid() {
        return (null != m_imageList && 0 != m_imageList.size());
    }

    public int getId() {
        return m_groupId;
    }

    public void setGrilId(int grilId) {
        this.m_groupId = grilId;
    }

    public String getHomePage() {
        return m_homePage;
    }

    public void setHomePage(String homePage) {
        this.m_homePage = homePage;
    }

    public String getTitle() {
        return m_title;
    }

    public void setTitle(String title) {
        this.m_title = title;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        if (!TextUtils.isEmpty(description) && !description.startsWith("null")) {
            this.m_description = description;
        }
    }

    public String getDate() {
        return m_date;
    }

    public void setDate(String date) {
        this.m_date = date;
    }

    public String getThumbUrl() {
        return m_thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.m_thumbUrl = thumbUrl;
    }

    public boolean hasFavorite() {
        return m_hasFavorite;
    }

    public void setHasFavorite(boolean isFavorite) {
        this.m_hasFavorite = isFavorite;
    }

    public boolean isReaded() {
        return m_isReaded;
    }

    public void setIsReaded(boolean isReaded) {
        m_isReaded = isReaded;
    }

    public String getTag() {
        return m_tag;
    }

    public void setTag(String tag) {
        m_tag = tag;
    }

    public String getCategory() {
        return m_category;
    }

    public void setCagetory(String category) {
        m_category = category;
    }

    public ArrayList<Image> getImageList() {
        return m_imageList;
    }

    public void setImageList(ArrayList<Image> imageList) {
        this.m_imageList = imageList;

        int size = (null != m_imageList) ? m_imageList.size() : 0;
        Image image = null;
        boolean hasFavImage = false;
        for (int ix = 0; ix < size; ix++) {
            image = m_imageList.get(ix);
            initImageProperties(image, ix);
            if (image.isFavorite()) {
                hasFavImage = true;
            }
        }

        setImageCount(size);
        setHasFavorite(hasFavImage);
        setHasInited(true);
    }

    public void addImage(String imageUrl, String imageThumbUrl) {
        if (null != imageUrl && null != imageThumbUrl && !imageUrl.isEmpty() && !imageThumbUrl.isEmpty()) {
            Image image = new Image(imageUrl, imageThumbUrl);
            m_imageList.add(image);
            initImageProperties(image, m_imageList.size() - 1);

            if (image.isFavorite()) {
                m_hasFavorite = true;
            }
        }
    }

    public void addImage(Image image) {
        if (null != image) {
            this.m_imageList.add(image);
            initImageProperties(image, m_imageList.size() - 1);

            if (image.isFavorite()) {
                m_hasFavorite = true;
            }
        }
    }

    public int getImageCount() {
        return m_imageCount;
    }

    public void setImageCount(int count) {
        if (count >= 0) {
            m_imageCount = count;
        }
    }

    public boolean hasInited() {
        return m_hasInited;
    }

    public void setHasInited(boolean hasInited) {
        m_hasInited = hasInited;
    }

    public int getImageIndex(Image image) {
        // May has the performance problem.
        return m_imageList.indexOf(image);
    }

    public String getRootDownloadDir() {
        if (TextUtils.isEmpty(m_downloadDir)) {
            m_downloadDir = DOWNLOAD_ROOT_DIR + "/" + m_title;
        }

        return m_downloadDir;
    }

    public String getRootThumbDownloadDir() {
        if (TextUtils.isEmpty(m_downloadThumbDir)) {
            m_downloadThumbDir = getRootDownloadDir() + "/" + DOWNLOAD_THUMB_DIR_NAME;
        }

        return m_downloadThumbDir;
    }

    public String getGroupDBString() {
        return "(" + 
                "'" + m_homePage      + "'" + ", " +
                "'" + m_title         + "'" + ", " +
                "'" + m_description   + "'" + ", " +
                "'" + m_thumbUrl      + "'" + ", " +
                ")";
    }

    public String getGroupImagesDBString() {
        if (null != m_imageList && 0 != m_imageList.size()) {
            StringBuilder dbString = new StringBuilder();
            String temp = "";

            for (Image image : m_imageList) {
                temp = "("  + m_groupId            + ", " +
                        "'" + image.getImageUrl()  + "'" + ", " +
                        "'" + image.getThumbUrl()  + "'" + "), ";

                dbString.append(temp);
            }

            return dbString.toString();
        }

        return "";
    }

    @Override
    public String toString() {
        return "* " + m_groupId     + " | " +
                      m_homePage    + " | " +
                      m_title       + " | " + 
                      m_description + " | " + 
                      m_thumbUrl    + " | ";
    }

    // private static final String s_nameIdRegex = "^http:.*#p=(.*)";
    // private static final Pattern s_nameIdPattern = Pattern.compile(s_nameIdRegex);

    private void initImageProperties(Image image, int index) {
        image.setParent(this);

        // TODO: It'll take too much memory?
        // I do a test, it'll take 2.5M memory. It should be lazy initialize?
        image.setImageDownloadPath(getPath(true, index, DOWNLOAD_FILE_SUFFIX));
        image.setImageThumbDownloadPath(getPath(false, index, DOWNLOAD_FILE_SUFFIX));
    }

    private String getPath(boolean isImage, int index, String suffix) {
        String rootPath = isImage ? getRootDownloadDir() : getRootThumbDownloadDir();
        return rootPath + "/" + index + suffix;
    }
}
