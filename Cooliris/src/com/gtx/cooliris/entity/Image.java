package com.gtx.cooliris.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Image
{
    private int     mImageId       	= 0;
    private int     mParentGroupId 	= 0;
    private String  mImageUrl      	= "";
    private String  mThumbUrl 		= "";
    private boolean mIsFavorite		= false;
    private WeakReference<ImageGroup> mParent = null;
    
    // TODO: TO be removed
    private String  mImageDownloadPath  	= "";
    private String  mImageThumbDownloadPath = "";
    
	public Image() {
	}

	public Image(String imageUrl, String thumbUrl) {
		this(0, imageUrl, thumbUrl);
	}

	public Image(int imageId, String imageUrl, String thumbUrl) {
		mImageId = imageId;
		mImageUrl = imageUrl;
		mThumbUrl = thumbUrl;
	}

	public ImageGroup getParent() {
		if (null != mParent) {
			return mParent.get();
		}

		return null;
	}

	public void setParent(ImageGroup group) {
		if (null != group) {
			mParent = new WeakReference<ImageGroup>(group);
		}
	}

	public int getGroupId() {
		return mParentGroupId;
	}

	public void setGroupId(int groupId) {
		this.mParentGroupId = groupId;
	}

	public int getImageId() {
		return mImageId;
	}

	public void setImageId(int imageId) {
		this.mImageId = imageId;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.mImageUrl = imageUrl;
	}

	public String getThumbUrl() {
		return mThumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.mThumbUrl = thumbUrl;
	}

	public boolean isFavorite() {
		return mIsFavorite;
	}

	public void setIsFavorite(boolean isFavorite) {
		this.mIsFavorite = isFavorite;

		ImageGroup parentGroup = getParent();
		// Notify parent's favorite state
		if (!mIsFavorite) {
			ArrayList<Image> images = parentGroup.getImageList();
			if (null != images && images.size() > 0) {
				boolean hasFavImage = false;
				for (Image image : images) {
					if (image.isFavorite()) {
						hasFavImage = true;
						break;
					}
				}

				parentGroup.setHasFavorite(hasFavImage);
			}
		} else {
			parentGroup.setHasFavorite(true);
		}
	}

	public String getImageDownloadPath() {
		return mImageDownloadPath;
	}

	public void setImageDownloadPath(String imageDownloadPath) {
		this.mImageDownloadPath = imageDownloadPath;
	}

	public String getImageThumbDownloadPath() {
		return mImageThumbDownloadPath;
	}

	public void setImageThumbDownloadPath(String imageThumbDownloadPath) {
		this.mImageThumbDownloadPath = imageThumbDownloadPath;
	}
}
