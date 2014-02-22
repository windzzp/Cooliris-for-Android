package com.gtx.cooliris.db;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.gtx.cooliris.constant.DBTableConstant;
import com.gtx.cooliris.constant.TAGConstant;
import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.utils.LogUtil;

public class ImagesTable extends AbstractDB
{
    private static final String TAG = "ImageGroupTable";
    
    public ImagesTable()
    {
        super();
    }
    
    public ArrayList<Image> getGroupImages(int groupId)
    {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Image> images = new ArrayList<Image>();
        
        if (null != db)
        {
            String sql = "SELECT * FROM Images WHERE article=" + groupId;
            Cursor csr = null;
            
            long start = System.currentTimeMillis();
            db.beginTransaction();
            try
            {
                csr = db.rawQuery(sql, null);
                if (null != csr && csr.getCount() > 0)
                {
                    Image image = null;
                    
                    while (csr.moveToNext())
                    {                        
                        image = new Image();
                        image.setImageId(csr.getInt(DBTableConstant.TAB_IMAGE_ID_IX));
                        image.setGroupId(groupId);
                        image.setImageUrl(csr.getString(DBTableConstant.TAB_IMAGE_URL_IX));
                        image.setIsFavorite(!(0 == csr.getInt(DBTableConstant.TAB_IMAGE_IS_FAVORITE_IX)));
                        
                        //image.setImageThumbUrl(csr.getString(DBTableConstant.TAB_IMAGE_THUMB_URL_IX));
                        //image.setImageDownloadPath(csr.getString(DBTableConstant.TAB_IMAGE_DOWNLOAD_PATH_IX));
                        //image.setImageThumbDownloadPath(csr.getString(DBTableConstant.TAB_IMAGE_THUMB_DOWNLOAD_PATH_IX));
                        
                        images.add(image);
                    }
                    
                    db.setTransactionSuccessful();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(csr);

                // TODO: Need close db? 
                // closeDB(db);
                
                db.endTransaction();
            }
            
            LogUtil.i("Performance", "Takes time = " + (System.currentTimeMillis() - start));
        }
        
        return images;
    }
    
    public void fillGroupImages(int groupId, ArrayList<Image> images)
    {
        if (null == images)
        {
            LogUtil.e(TAGConstant.TAG_DB_OPERATION, "Fill Group Images failed!");
            return;
        }
        
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            String sql = "SELECT * FROM Images WHERE article=" + groupId;
            Cursor csr = null;
            
            //long start = System.currentTimeMillis();
            try
            {
                csr = db.rawQuery(sql, null);
                fillImagesInternal(csr, groupId, images);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(csr);

                // TODO: Need close db? 
                // closeDB(db);
            }
            
            //LogUtil.i("Performance", "Takes time = " + (System.currentTimeMillis() - start));
        }
    }
    
    public void fillGroupImages(ArrayList<ImageGroup> groups) {
        if (null == groups || 0 == groups.size()) {
            LogUtil.e(TAGConstant.TAG_DB_OPERATION, "Fill Group Images failed!");
            return;
        }
        
        // Get the image count for first
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (ImageGroup imageGroup : groups) {
        	ids.add(imageGroup.getId());
		}
        
        String idsRange = createInRange(ids);
        SparseIntArray imageCountMap = getImageCount(ids);
        
        SQLiteDatabase db = getReadableDatabase();

		if (null != db) {
			// select * from Images where article in (1,2,3) order by article
			String sql = "SELECT * FROM Images WHERE article IN " + idsRange + " order by article";
			Cursor csr = null;

			// long start = System.currentTimeMillis();
			try {
				csr = db.rawQuery(sql, null);
				if (null != csr) {

				}

				// fillImagesInternal(csr, groupId, images);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeCursor(csr);

				// TODO: Need close db?
				// closeDB(db);
			}

			// LogUtil.i("Performance", "Takes time = " +
			// (System.currentTimeMillis() - start));
		}
	}
    
    public void fillImagesInternal(Cursor csr, int groupId, ArrayList<Image> groupImages)
    {
        long start = System.currentTimeMillis();
        if (null != csr && csr.getCount() > 0 && groupImages.size() > 0)
        {
            int size = groupImages.size();
            int ix = 0;
            Image image = null;

            while (csr.moveToNext() && ix < size)
            {
                // Use the data which alloc before.
                image = groupImages.get(ix);
                
                image.setImageId(csr.getInt(DBTableConstant.TAB_IMAGE_ID_IX));
                image.setGroupId(groupId);
                image.setImageUrl(csr.getString(DBTableConstant.TAB_IMAGE_URL_IX));
                image.setThumbUrl(csr.getString(DBTableConstant.TAB_IMAGE_THUMB_URL_IX));
                image.setIsFavorite(!(0 == csr.getInt(DBTableConstant.TAB_IMAGE_IS_FAVORITE_IX)));
    
                ix++;
            }
        }

        LogUtil.i("Performance", "Takes time = " + (System.currentTimeMillis() - start));
    }    
    
    public ImageGroup getImageGroup(int groupId)
    {
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            String sql = "SELECT * FROM Images WHERE article=" + groupId;
            Cursor csr = null;
            
            try
            {
                csr = db.rawQuery(sql, null);
                if (null != csr && csr.getCount() > 0)
                {
                    int ix = 0;
                    ImageGroup group = new ImageGroup();
                    
                    
                    while (csr.moveToNext())
                    {
                        if (0 == ix++)
                        {
                            group.setGrilId(csr.getInt(DBTableConstant.TAB_IMAGE_PARENT_ID_IX));
                        }
                        
                        Image image = new Image();
                        image.setImageId(csr.getInt(DBTableConstant.TAB_IMAGE_ID_IX));
                        image.setImageUrl(csr.getString(DBTableConstant.TAB_IMAGE_URL_IX));
                        image.setThumbUrl(csr.getString(DBTableConstant.TAB_IMAGE_THUMB_URL_IX));
                        
                        // Add the image into girl
                        group.addImage(image);
                    }
                    
                    group.setHasInited(true);
                    
                    return group;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(csr);

                // TODO: Need close db? 
                // closeDB(db);
            }
        }
        
        return null;
    }
    
    public int getImageCount(int groupId)
    {
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            String sql = "SELECT COUNT (*) FROM Images WHERE article=" + groupId;
            Cursor csr = null;
            
            try
            {
                csr = db.rawQuery(sql, null);
                if (null != csr)
                {
                    while (csr.moveToNext())
                    {
                        return csr.getInt(0);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(csr);

                // TODO: Need close db? 
                // closeDB(db);
            }
        }
        
        return 0;
    }    
    
    public SparseIntArray getImageCount(ArrayList<Integer> ids)
    {
        SparseIntArray rtMap = new SparseIntArray((null == ids) ? 0 : ids.size());
        if (null == ids || 0 == ids.size())
        {
            return rtMap;
        }
        
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            // SELECT article, COUNT (*) FROM Images WHERE article in (2,3,5) group by article
            // Result:
            // article count(*)
            //   2       9
            //   3       14
            //   5       15
            // NOTE: If the id is invalid, it'll return nothing. That's why we should use HashMap.
            
            // Create ids range string
            String idsRange = createInRange(ids);
            
            // Create a sql string
            String sql = "SELECT article, COUNT (*) FROM Images WHERE article IN " + idsRange + " group by article";
            LogUtil.d(TAG, "query sql: " + sql);
            
            long start = System.currentTimeMillis();
            
            int groupId = 0;
            int itemCount = 0;
            Cursor c = null;
            try
            {
                c = db.rawQuery(sql, null);
                if (null != c && c.getCount() > 0)
                {
                    while (c.moveToNext())
                    {
                        groupId = c.getInt(0);
                        itemCount = c.getInt(1);
                        rtMap.put(groupId, itemCount);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(c);
                
                LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                        "Get image count by ids (count = " + ids.size() + 
                        ") takes time = " + (System.currentTimeMillis() - start));
            }
        }
        
        return rtMap;
    }
    
    public ArrayList<Integer> getFavoriteImageGroupIds()
    {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        if (null != db)
        {
            String sql = "select distinct article from Images where is_favorite=1";
            Cursor csr = null;
            
            try
            {
                csr = db.rawQuery(sql, null);
                if (null != csr && csr.getCount() > 0)
                {                    
                    while (csr.moveToNext())
                    {
                        // Get the column 0 because we only select one column 
                        ids.add(csr.getInt(0));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeCursor(csr);

                // TODO: Need close db? 
                // closeDB(db);
            }
        }
        
        return ids;
    }
    
    public int[] getFavoriteImageGroupIds2()
    {
    	int[] ids = null;
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            String sql = "select distinct article from Images where is_favorite=1";
            
            Cursor c = null;
            long start = System.currentTimeMillis();
			try {
				c = db.rawQuery(sql, null);
				
				if (null != c) {
					int count = c.getCount();
					if (count > 0) {
						ids = new int[count];
						int index = 0;
						while (c.moveToNext()) {
							ids[index++] = c.getInt(0);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
                closeCursor(c);

                LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                		"Get image group Ids by category [" + "Favorite" + 
                		"] takes time = " + (System.currentTimeMillis() - start));
            }
        }
        
        return ids;
    }
    
    public void updateFavorite(Image image)
    {
    	if (null == image)
		{
			return;
		}
        SQLiteDatabase db = getWritableDatabase();
        int image_id = image.getImageId();
        int isFavorite = image.isFavorite() ? 1 : 0;
        
        if (null != db)
        {
            String sql = "update Images set is_favorite=" + isFavorite + " where _id=" + image_id;
            
            try
            {
            	db.execSQL(sql);
            }
            catch (SQLException e)
            {
            	e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                // TODO: Need close db? 
                // closeDB(db);
            }
        }
    }
    
    public void updateDownloadPath(Image image)
    {
    	if (null == image)
		{
			return;
		}
        SQLiteDatabase db = getWritableDatabase();
        //int image_id = image.getImageId();
        String imageUrl = image.getImageUrl();
        String downloadPath = image.getImageDownloadPath();
        
        if (null != db && !TextUtils.isEmpty(downloadPath))
        {
            String sql = "update Images set image_download_path='" + downloadPath + "' where _id='" + imageUrl + "'";
            
            try
            {
            	db.execSQL(sql);
            }
            catch (SQLException e)
            {
            	e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                // TODO: Need close db? 
                // closeDB(db);
            }
        }
    }
    
    public void removeItem(Image image)
    {
        if (null == image)
        {
            return;
        }
        
        SQLiteDatabase db = getWritableDatabase();
        String imageUrl = image.getImageUrl();
        int imageId = image.getImageId();
        
        if (null != db && !TextUtils.isEmpty(imageUrl))
        {
            String sql = "delete from Images where image_id='" + imageId + "'";
            LogUtil.i(TAGConstant.TAG_DB_OPERATION, "SQL: " + sql);
            
            try
            {
                db.execSQL(sql);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                // TODO: Need close db? 
                // closeDB(db);
            }
        }
    }
    
    private String createInRange(ArrayList<Integer> ids)
    {
        // Create ids range string
        StringBuilder idsRange = new StringBuilder();
        idsRange.append("(");
        int size = ids.size();
        for (int ix = 0; ix < size; ix++)
        {
            idsRange.append(ids.get(ix).toString());
            if (ix != size - 1)
            {
                idsRange.append(",");
            }
        }
        idsRange.append(")");
        
        return idsRange.toString();
    }
}
