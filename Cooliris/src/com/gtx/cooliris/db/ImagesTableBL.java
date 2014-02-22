package com.gtx.cooliris.db;

import java.util.ArrayList;

import android.util.SparseIntArray;

import com.gtx.cooliris.constant.TAGConstant;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.utils.LogUtil;

public class ImagesTableBL {
    private static ImagesTableBL s_instance = new ImagesTableBL();
    private static ImagesTable s_db = new ImagesTable();

    public static ImagesTableBL getInstance() {
        return s_instance;
    }

    private ImagesTableBL() {
    }

    public ArrayList<Image> getGroupImages(int groupId) {
        return s_db.getGroupImages(groupId);
    }

    public void loadGroupImages(ImageGroup group) {
        ArrayList<Image> images = s_db.getGroupImages(group.getId());
        group.setImageList(images);

        /*if (null != images) {
            // NOTE: Remove the last image because the image is the netease's LOGO image
            GirlImage lastImage = images.remove(images.size() - 1);

            // Set the images into girl
            girl.setImageList(images);

            s_db.removeItem(lastImage);

            GirlsDBBL.getInstance().updateGirlImageCount(girl);
        }*/
    }

    public void fillGroupsImages(ArrayList<ImageGroup> groups) {

    }

    public void fillGroupImages(ImageGroup group) {
        synchronized (s_instance) {
            if (null != group && !group.hasInited()) {
                ArrayList<Image> images = group.getImageList();
                if (null != images && images.size() > 0) {
                    s_db.fillGroupImages(group.getId(), images);
                    group.setHasInited(true);
                } else {
                    // May not be happend
                    LogUtil.e(TAGConstant.TAG_DB_OPERATION,
                            "fill group images has some error! ID = " + group.getTitle());
                    images = s_db.getGroupImages(group.getId());
                    group.setImageList(images);
                }
            } else {
                LogUtil.e(TAGConstant.TAG_DB_OPERATION, "Has initialize = " + group.hasInited());
            }
        }
    }
    
    public int getImageCount(int groupId) {
        /*ArrayList<Integer> ids = new ArrayList<Integer>(1);
        ids.add(groupId);

        HashMap<Integer, Integer> rtMap = getImageCount(ids);
        return rtMap.get(groupId);*/

        return s_db.getImageCount(groupId);
    }

    public SparseIntArray getImageCount(ArrayList<Integer> ids) {
        return s_db.getImageCount(ids);
    }

    // TODO: TBD
    public ArrayList<Integer> getFavoriteImageGroupIds() {
        return s_db.getFavoriteImageGroupIds();
    }

    public int[] getFavoriteImageGroupIds2() {
        return s_db.getFavoriteImageGroupIds2();
    }

    public ImageGroup getImageGroup(int groupId) {
        return s_db.getImageGroup(groupId);
    }

    public void updateFavorite(Image image) {
        s_db.updateFavorite(image);
    }

    public void updateDownloadPath(Image image) {
        s_db.updateDownloadPath(image);
    }
}
