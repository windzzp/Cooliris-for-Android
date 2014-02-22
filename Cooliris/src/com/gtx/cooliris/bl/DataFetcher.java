package com.gtx.cooliris.bl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.gtx.cooliris.environment.NetworkEnvironment;

public class DataFetcher
{
    private static DataFetcher s_instance = new DataFetcher();
    
    public static DataFetcher getInstance()
    {
        return s_instance;
    }
    
    public String getGirlsListXML()
    {
        HttpURLConnection conn = null;
        InputStream is = null;
        try
        {
            URL url = new URL(NetworkEnvironment.REQUEST_TYPE_GIRL_LIST);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(NetworkEnvironment.CONNECT_TIMEOUT);
            conn.setReadTimeout(NetworkEnvironment.SOCKET_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int responseCode = conn.getResponseCode();
            Log.d("Girls", "Response code: " + responseCode);

            if (responseCode == HttpStatus.SC_OK)
            {
                is = conn.getInputStream();
                int len = conn.getContentLength();
                String encoding = conn.getContentEncoding();
                Log.e("Girls", "Content length = " + len);
                encoding = ((null == encoding) || (0 == encoding.length())) ? "UTF-8" : encoding;

                String contentAsString = readStream(is, len, encoding);
                return contentAsString;
            }

            conn.disconnect();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            /*
             * if (null != conn) { conn.disconnect(); }
             */
        }

        return "";
    }
    
    public String getGirlImagesXML(int girlId)
    {
        HttpURLConnection conn = null;
        InputStream is = null;
        try
        {
            URL url = new URL(NetworkEnvironment.REQUEST_TYPE_GIRL_LIST);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(NetworkEnvironment.CONNECT_TIMEOUT);
            conn.setReadTimeout(NetworkEnvironment.SOCKET_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            
            conn.addRequestProperty("RequestCode", "GetGirlImage");
            conn.addRequestProperty("girl_id", Integer.toString(girlId));
            
//            conn.setRequestMethod("setRequestMethod");
//            conn.setRequestProperty("ka", "va");

            conn.connect();
            int responseCode = conn.getResponseCode();
            Log.d("Girls", "Response code: " + responseCode);

            if (responseCode == HttpStatus.SC_OK)
            {
                is = conn.getInputStream();
                int len = conn.getContentLength();
                String encoding = conn.getContentEncoding();
                Log.e("Girls", "Content length = " + len);
                encoding = ((null == encoding) || (0 == encoding.length())) ? "UTF-8" : encoding;

                String contentAsString = readStream(is, len, encoding);
                return contentAsString;
            }

            conn.disconnect();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            /*
             * if (null != conn) { conn.disconnect(); }
             */
        }

        return "";
    }

    public String readStream(InputStream is, int contentLength, String encoding)
    {
        if (null == is)
        {
            return "";
        }
        
        final int bufferSize = 1024 * 8;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(contentLength);
        try
        {
            byte[] buffer = new byte[bufferSize];
            int len = -1;
            while (-1 != (len = is.read(buffer)))
            {
                baos.write(buffer, 0, len);
            }
            
            return new String(baos.toByteArray(), encoding);            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                baos.close();
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return "";
    }
}
