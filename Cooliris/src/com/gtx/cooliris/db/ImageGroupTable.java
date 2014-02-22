package com.gtx.cooliris.db;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.gtx.cooliris.constant.DBTableConstant;
import com.gtx.cooliris.constant.TAGConstant;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.ImageGroupCategory;
import com.gtx.cooliris.utils.LogUtil;

public class ImageGroupTable extends AbstractDB
{
    private static final String TAG = "ImageGroupTable";
    
    public ImageGroupTable()
    {
        super();
    }
    
    public ArrayList<ImageGroup> getAllGroups()
    {
        ArrayList<ImageGroup> imageGroups = new ArrayList<ImageGroup>();
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            String sql = "SELECT * FROM Articles";
            Cursor c = null;
            long start = System.currentTimeMillis();
            try
            {
                c = db.rawQuery(sql, null);
                if (null != c && c.getCount() > 0)
                {
                    while (c.moveToNext())
                    {
                        // Create a image group
                        ImageGroup group = new ImageGroup();
                        
                        // Fill image group data
                        fillGroupData(c, group);
                        
                        // Add to list
                        imageGroups.add(group);
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

                // TODO: Need close db? 
                // closeDB(db);
            }
            
            LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                    "Get all groups takes time = " + (System.currentTimeMillis() - start));
        }
        
        return imageGroups;
    }
    
    public int[] loadGroupIds(int category)
    {
    	int[] ids = null;
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db) {
            String sql = "select _id from articles ";
            
            /**
             * If the category type is {@link ImageGroupCategory#CATEGORY_ALL} , 
             * we should not add the query condition.
             */
            String categoryName = ImageGroupCategory.getCategoryName(category);
        	if (ImageGroupCategory.CATEGORY_ALL != category) {
        		sql = sql + " where category like " + "'%" + categoryName + "%'";
			}
            
            LogUtil.i(TAGConstant.TAG_PERFORMANCE, "sql = " + sql);
            
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
                		"Get image group Ids by category [" + categoryName + 
                		"] takes time = " + (System.currentTimeMillis() - start));
            }
        }
        
        return ids;
    }
    
    public ArrayList<ImageGroup> getGroupsByCategory(String category)
    {
        ArrayList<ImageGroup> imageGroups = new ArrayList<ImageGroup>();
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            // SELECT * FROM Articles where category like '%Áé¸Ð%'
            String sql = "SELECT * FROM Articles where category like " + "'%" + category + "%'";
            Cursor c = null;
            long start = System.currentTimeMillis();
            try
            {
                c = db.rawQuery(sql, null);
                if (null != c && c.getCount() > 0)
                {
                    while (c.moveToNext())
                    {
                        // Create a image group
                        ImageGroup group = new ImageGroup();
                        
                        // Fill image group data
                        fillGroupData(c, group);
                        
                        // Add to list
                        imageGroups.add(group);
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

                // TODO: Need close db? 
                // closeDB(db);
            }
            
            LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                    "Get image groups by category [" + category + 
                    "] takes time = " + (System.currentTimeMillis() - start));
        }
        
        return imageGroups;
    }
    
    public ArrayList<ImageGroup> loadGroupsByIds(ArrayList<Integer> ids)
    {
        if (null == ids || 0 == ids.size())
        {
            return new ArrayList<ImageGroup>();
        }
        
        ArrayList<ImageGroup> imageGroups = new ArrayList<ImageGroup>();
        SQLiteDatabase db = getReadableDatabase();
        
        if (null != db)
        {
            // Create ids range string
            String idsRange = createInRange(ids);
            
            // Create a sql string
            String sql = "SELECT * FROM Articles where _id IN " + idsRange;
            LogUtil.d(TAG, "query sql: " + sql);
            
            Cursor c = null;
            long start = System.currentTimeMillis();
            try
            {
                c = db.rawQuery(sql, null);
                if (null != c && c.getCount() > 0)
                {
                    while (c.moveToNext())
                    {
                        // Create a image group
                        ImageGroup group = new ImageGroup();
                        
                        // Fill image group data
                        fillGroupData(c, group);
                        
                        // Add to list
                        imageGroups.add(group);
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
            }
            
            LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                    "Get image groups by ids (count = " + ids.size() + 
                    ") takes time = " + (System.currentTimeMillis() - start));
        }
        
        return imageGroups;
    }
    
    public SparseArray<ImageGroup> loadGroupsByIds(int[] ids)
 {
		int size = (null == ids) ? 0 : ids.length;
		SparseArray<ImageGroup> imageGroups = new SparseArray<ImageGroup>(size);

		if (0 == size) {
			return imageGroups;
		}

		SQLiteDatabase db = getReadableDatabase();

		if (null != db) {
			// Create ids range string
			String idsRange = createInRange(ids);

			// Create a sql string
			String sql = "SELECT * FROM Articles where _id IN " + idsRange;
			LogUtil.d(TAG, "query sql: " + sql);

			Cursor c = null;
			long start = System.currentTimeMillis();
			
			try {
				c = db.rawQuery(sql, null);
				if (null != c && c.getCount() > 0) {
					
					while (c.moveToNext()) {
						// Create a image group
						ImageGroup group = new ImageGroup();

						// Fill image group data
						fillGroupData(c, group);

						// Add to list
						imageGroups.put(group.getId(), group);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeCursor(c);
			}
            
            LogUtil.i(TAGConstant.TAG_PERFORMANCE, 
                    "Get image groups by ids (count = " + ids.length + 
                    ") takes time = " + (System.currentTimeMillis() - start));
        }
        
        return imageGroups;
    }
        
    
//    public void updateImageGroupCount(ImageGroup group)
//    {
//        if (null != group)
//        {
//            SQLiteDatabase db = getWritableDatabase();
//            int girlId = group.getGirlId();
//            int count = group.getImageCount();
//
//            String sql = "update girls set image_count='" + count + "' where girl_id='" + girlId + "'";
//            LogUtil.i(TAGConstant.TAG_DB_OPERATION, "SQL: " + sql);
//
//            try
//            {
//                db.execSQL(sql);
//            }
//            catch (SQLException e)
//            {
//                e.printStackTrace();
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            finally
//            {
//                // TODO: Need close db?
//                // closeDB(db);
//            }
//        }
//    }
    
    public void fillGroupData(Cursor c, ImageGroup group)
    {
        if (null != c && null != group)
        {
            group.setGrilId(c.getInt(DBTableConstant.TAB_IMAGEGROUP_GIRL_ID_IX));
            group.setHomePage(c.getString(DBTableConstant.TAB_IMAGEGROUP_HOME_PAGE_IX));
            group.setTitle(c.getString(DBTableConstant.TAB_IMAGEGROUP_TITLE_IX));
            group.setDescription(c.getString(DBTableConstant.TAB_IMAGEGROUP_DESCRIPTION_IX));
            group.setThumbUrl(c.getString(DBTableConstant.TAB_IMAGEGROUP_THUMB_URL_IX));
            //group.setImageCount(c.getInt(DBTableConstant.TAB_GIRLS_THUMB_IMAGE_COUNT_IX)); // no use
            
            group.setIsReaded(1 == c.getInt(DBTableConstant.TAB_IMAGEGROUP_IS_READED_IX));
            group.setDate(c.getString(DBTableConstant.TAB_IMAGEGROUP_TIME_IX));
            group.setTag(c.getString(DBTableConstant.TAB_IMAGEGROUP_TAG_IX));
            group.setCagetory(c.getString(DBTableConstant.TAB_IMAGEGROUP_CATEGORY_IX));
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
    
    private String createInRange(int[] ids)
    {
        // Create ids range string
        StringBuilder idsRange = new StringBuilder();
        idsRange.append("(");
        int size = ids.length;
        for (int ix = 0; ix < size; ix++)
        {
            idsRange.append(ids[ix]);
            if (ix != size - 1)
            {
                idsRange.append(",");
            }
        }
        idsRange.append(")");
        
        return idsRange.toString();
    }    
}
