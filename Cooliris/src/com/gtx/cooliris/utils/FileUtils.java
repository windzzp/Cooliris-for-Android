package com.gtx.cooliris.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.gtx.cooliris.imagecache.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;

/**
 * This file provides method to operate file input stream or output stream.
 * 
 */
public class FileUtils
{
    /**
     * Default buffer size while operate file.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    /**
     * Copy the source file into destination.
     * 
     * @param srcFile Specified the source file path.
     * @param destFile Specified the destination file path.
     * 
     * @return If copy success, return true; Otherwise, return false.
     */
    public static boolean copyFile(String srcFile, String destFile)
    {
        if (null == srcFile || null == destFile || 0 == srcFile.length() || 0 == destFile.length()
                || srcFile.equals(destFile))
        {
            return false;
        }

        File src = new File(srcFile);
        File dest = new File(destFile);

        // Check the source file is exist
        if (!src.exists())
        {
            return false;
        }

        // Check the destination folder is exist, if not, create the folder.
        File destDir = new File(dest.getParent());
        if (!destDir.exists())
        {
            if (!destDir.mkdirs())
            {
                return false;
            }
        }

        // Copy file into the destination folder.
        try
        {
            InputStream is = new FileInputStream(src);
            OutputStream os = new FileOutputStream(dest);
            return copyStream(is, os);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Copy the source stream into destination.
     * 
     * @param is Specified input stream.
     * @param os Specified output stream.
     * 
     * @return If copy success, return true; Otherwise, return false.
     */
    public static boolean copyStream(InputStream is, OutputStream os)
    {
        if (null == is || null == os)
        {
            return false;
        }

        try
        {
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            int len = 0;
            while ((len = is.read(buf)) > 0)
            {
                os.write(buf, 0, len);
            }
            is.close();
            os.close();

            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }
    
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Return the size of a directory/file in bytes.
	 */
	public static long getFileOrFolderSize(File fileOrFolder) {
	    if (null != fileOrFolder && fileOrFolder.exists()) {
	    	
	    	if (fileOrFolder.isDirectory()) {
	    		long result = 0;
	    		File[] fileList = fileOrFolder.listFiles();
	    		for(int i = 0; i < fileList.length; i++) {
	    			// Recursive call if it's a directory
	    			if(fileList[i].isDirectory()) {
	    				result += getFileOrFolderSize(fileList [i]);
	    			} else {
	    				// Sum the file size in bytes
	    				result += fileList[i].length();
	    			}
	    		}
	    		return result; // return the file size
			} else {
				return fileOrFolder.length();
			}
	    }
	    return 0;
	}
	
    public static File getDiskCacheDir(Context context) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
		File cachePath = (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()) ? 
				getExternalCacheDir(context) : context.getCacheDir();
				
		return cachePath;
    }
    
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }
    
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }    
}
