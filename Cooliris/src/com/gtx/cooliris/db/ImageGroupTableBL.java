package com.gtx.cooliris.db;

import java.util.ArrayList;

import android.util.SparseArray;

import com.gtx.cooliris.entity.ImageGroup;

public class ImageGroupTableBL
{
    private static ImageGroupTableBL s_instance = new ImageGroupTableBL();
    
    private ImageGroupTable m_db = new ImageGroupTable();
    
    public static ImageGroupTableBL getInstance()
    {
        return s_instance;
    }
    
    private ImageGroupTableBL()
    {
    }
    
    public ArrayList<ImageGroup> loadAllGroups()
    {
        return m_db.getAllGroups();
    }
    
    public int[] loadGroupIds(int category)
    {
        return m_db.loadGroupIds(category);
    }
    
    public ArrayList<ImageGroup> loadGroupsByCategory(String category)
    {
        return m_db.getGroupsByCategory(category);
    }
    
    // TODO: TBDelete
    public ArrayList<ImageGroup> loadImageGroupsByIds(ArrayList<Integer> ids)
    {
        return m_db.loadGroupsByIds(ids);
    }
    
    public SparseArray<ImageGroup> loadImageGroupsByIds2(ArrayList<Integer> ids)
    {
    	int[] tempIds = new int[ids.size()];
    	int ix = 0;
    	for (Integer id : ids) {
			tempIds[ix++] = id;
		}
    	
        return m_db.loadGroupsByIds(tempIds);
    }
    
    public SparseArray<ImageGroup> loadImageGroupsByIds(int[] ids)
    {
        return m_db.loadGroupsByIds(ids);
    }   
//    public void updateGirlImageCount(ImageGroup girl)
//    {
//        s_db.updateImageGroupCount(girl);
//    }
}
