package com.gtx.cooliris.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.gtx.cooliris.entity.Image;
import com.gtx.cooliris.entity.ImageGroup;

public class DownloadHelper
{
	private static final String TAG = "DownloadHelper";

	private static final String DOWNLOAD_DIR_NAME = "Cooliris";
	private static final File DOWNLOAD_ROOT_DIR = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DOWNLOAD_DIR_NAME);
	
	/*private static final File DOWNLOAD_ROOT_DIR = 
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);*/
	
	private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int CONNECT_TIMEOUT = 3 * 1000;
    private static final int SOCKET_TIMEOUT = 5 * 1000;
	
	public static File getDownloadDir()
	{
		return DOWNLOAD_ROOT_DIR;
	}
	
    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
    
    public static void downloadGirlImagesInGroup(ImageGroup girl)
    {
    	downloadGirlImage(girl, -1);
    }
    
    public static void downloadGirlImage(ImageGroup girl, final int index)
    {
    	if (null == girl)
		{
			return;
		}
    	
    	final ArrayList<Image> images = girl.getImageList();
    	final int size = images.size();
    	
    	if (index >= size || index < -1)
		{
			return;
		}
    	
    	// Create the download root directory with the specified girl info
    	String folderName = girl.getTitle();
    	final File rootFolder = new File(DOWNLOAD_ROOT_DIR, folderName);
    	if (null != rootFolder && !rootFolder.exists())
		{
			rootFolder.mkdirs();
		}
    	
    	// TODO: Should use thread pool?
    	// We do a temporary download, we should use thread pool to optimize our application
//    	new Thread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
				try
				{
					File file = null;
					FileOutputStream os = null;
					String url = null;
					Image image = null;
					if (-1 == index)
					{
						for (int ix = 0; ix < size; ix++)
						{
							image = images.get(ix);
							url = image.getImageUrl();
							file = new File(rootFolder, Integer.toString(ix) + ".jpg");
							if (!file.exists())
							{
								os = new FileOutputStream(file);
								if (downloadUrlToStream(url, os))
								{
									image.setImageDownloadPath(file.getAbsolutePath());
									// Save the download path to DB
									//GirlImagesDBBL.getInstance().updateDownloadPath(image);
								}
							}
							else 
							{
								LogUtil.d(TAG, "file is exist");
								image.setImageDownloadPath(file.getAbsolutePath());
							}
						}
					}
					else
					{
						image = images.get(index);
						url = images.get(index).getImageUrl();
						file = new File(rootFolder, Integer.toString(index) + ".jpg");
						if (!file.exists())
						{
							os = new FileOutputStream(file);
							if (downloadUrlToStream(url, os))
							{
								image.setImageDownloadPath(file.getAbsolutePath());
								
								// Save the download path to DB
								//GirlImagesDBBL.getInstance().updateDownloadPath(image);
							}
						}
						else 
						{
							LogUtil.e(TAG, "file is exist");
							image.setImageDownloadPath(file.getAbsolutePath());
						}
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
//			}
//		}).start();
    }
    
	public static boolean downloadUrlToStream(String urlString, FileOutputStream outputStream) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT);
            
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
					// TODO: Is that right?
                	out.flush();
                	outputStream.getFD().sync();
                	
                    out.close();
                    outputStream.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }
}
