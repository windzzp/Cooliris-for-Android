/*
 * System: Pascal
 * @version     1.00
 * 
 * Copyright (C) 2012, TOSHIBA Corporation.
 * 
 */

package com.gtx.cooliris.bl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;

import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.utils.FileUtils;

/**
 * This class is used to manage the resource of server information.
 */
public class ResourceManager
{
    /**
     * TAG
     */
    public static final String TAG  = "ResourceManager";
    
    /**
     * The database file name in asset resource.
     */
    //private static final String ASSETS_GIRLS_DB = "\\db\\girls.db";
    //private static final String GIRLS_DB_NAME = "girls.db";
    private static final String IMAGE_DB_NAME = "sudasuta.db";
    
    /**
     * The database file folder in asset resource.
     */
    private static final String IMAGE_DB_DIR = "databases";
    
    /**
     * Initialization.
     */
    public static void initialize()
    {
        File imageDBFile = getDBFile();
        if (null == imageDBFile || !imageDBFile.exists())
        {
            copyDBFile();
        }
    }
    
    public static boolean hasInitialized() 
    {
        File imageDBFile = getDBFile();
        return (null != imageDBFile && imageDBFile.exists());
	}
    
    public static void copyDBFile()
    {
        Context context = CoolirisApplication.getAppContext();
        
        try
        {
            ApplicationInfo appInfo = context.getApplicationInfo();
            String dataDir = appInfo.dataDir;
            File dbDir = new File(dataDir, IMAGE_DB_DIR);
            // Make sure the databases folder exists.
            if (!dbDir.exists())
            {
                dbDir.mkdirs();
            }
            
            AssetManager assetManager = context.getAssets();
            File outFile = new File(dbDir, IMAGE_DB_NAME);
            InputStream is = assetManager.open(IMAGE_DB_DIR + "/" + IMAGE_DB_NAME);
            FileOutputStream os = new FileOutputStream(outFile);
            FileUtils.copyStream(is, os);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            
        }
    }
    
    public static File getDBFile()
    {
        Context context = CoolirisApplication.getAppContext();
        ApplicationInfo appInfo = context.getApplicationInfo();
        String dataDir = appInfo.dataDir + "/" + IMAGE_DB_DIR;
        
        return new File(dataDir, IMAGE_DB_NAME);
    }
}
