package com.gtx.cooliris.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

public class BitmapUtil
{
    /**
     * Get rounded corner bitmap from specified Bitmap.
     * 
     * @param bmpSrc The source Bitmap object.
     * @param rx     The radius of horizontal direction.
     * @param ry     The radius of vertical direction.
     * 
     * @return The rounded corner bitmap object, null will be returned if this method failed.
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bmpSrc, float rx, float ry)
    {
        if (null == bmpSrc)
        {
            return null;
        }

        int bmpSrcWidth  = bmpSrc.getWidth();
        int bmpSrcHeight = bmpSrc.getHeight();

        try
        {
            if (bmpSrcWidth > 0 && bmpSrcHeight > 0)
            {
                Bitmap bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Config.ARGB_8888);
                if (null != bmpDest)
                {
                    Canvas canvas = new Canvas(bmpDest);
                    final int color = 0xff424242;
                    final Paint paint = new Paint();
                    final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);
                    final RectF rectF = new RectF(rect);

                    // Setting or clearing the ANTI_ALIAS_FLAG bit AntiAliasing smooth out
                    // the edges of what is being drawn, but is has no impact on the interior of the shape.
                    paint.setAntiAlias(true);

                    canvas.drawARGB(0, 0, 0, 0);
                    paint.setColor(color);
                    canvas.drawRoundRect(rectF, rx, ry, paint);
                    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                    canvas.drawBitmap(bmpSrc, rect, rect, paint);
                }
        
                return bmpDest;
            }
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Duplicate a bitmap by specified source bitmap.
     * 
     * @param bmpSrc The specified bitmap.
     * 
     * @return The duplicate bitmap.
     */
    public static Bitmap duplicateBitmap(Bitmap bmpSrc)
    {
        if (null == bmpSrc)
        {
            return null;
        }
        
        int bmpSrcWidth  = bmpSrc.getWidth();
        int bmpSrcHeight = bmpSrc.getHeight();
        
        if (0 == bmpSrcWidth || 0 == bmpSrcHeight)
        {
            return null;
        }

        Bitmap bmpDest = null;
        
        try
        {
            bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Config.ARGB_8888);
            if (null != bmpDest)
            {
                Canvas canvas = new Canvas(bmpDest);
                final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);
                
                canvas.drawBitmap(bmpSrc, rect, rect, null);
            }
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return bmpDest;
    }
    
    /**
     * Convert a picture object to bitmap.s
     * 
     * @param pic       The object of picture..
     *
     * @return          The target bitmap.
     */
    public static Bitmap pictureToBitmap(Picture pic)
    {
        Bitmap bmp = null;
        
        if (null != pic)
        {
            int w = pic.getWidth();
            int h = pic.getHeight();
         
            try
            {
                bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
                if (null != bmp)
                {
                    Canvas canvas = new Canvas(bmp);
                    pic.draw(canvas);
                }
            }
            catch (OutOfMemoryError e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return bmp;
    }
    
    /**
     * Returns an immutable bitmap from the specified subset of the source bitmap. 
     * 
     * @param bitmap    The source bitmap.
     * @param wScale    X axis scale.
     * @param hScale    Y axis scale.
     * 
     * @return          The target bitmap.
     */
    public static Bitmap getScaleBitmap(Bitmap bitmap, float wScale, float hScale)
    {
        try
        {
            Matrix matrix = new Matrix();
            matrix.postScale(wScale, hScale);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmp;
        }
        catch (OutOfMemoryError e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Create sized bitmap.
     * 
     * @param bitmap    The source bitmap.
     * @param width     The width of new bitmap.
     * @param height    The height of new bitmap.
     * 
     * @return          The target bitmap.
     */
    public static Bitmap getSizedBitmap(Bitmap bitmap, int dstWidth, int dstHeight)
    {
        if (null != bitmap)
        {
            try
            {
                if (dstWidth > 0 && dstHeight > 0)
                {
                    Bitmap result = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
                    return result;
                }
            }
            catch (OutOfMemoryError e)
            {
                // TODO: handle exception
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return bitmap;
    }
    
    /**
     * Returns an full screen bitmap from the specified subset of the source bitmap. 
     * 
     * @param bitmap    The source bitmap.
     * @param wScale    X axis scale.
     * @param hScale    Y axis scale.
     * 
     * @return          The target bitmap.
     */
    public static Bitmap getFullScreenBitmap(Bitmap bitmap, int wScale, int hScale)
    {
        int dstWidth  = bitmap.getWidth() * wScale;
        int dstHeight = bitmap.getHeight() * hScale;
        Bitmap result = null;
        
        try
        {
            result = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Convert a byte array to a bitmap.
     * 
     * @param array     The byte array that to be convert.
     * 
     * @return          The bitmap.
     */
    public static Bitmap byteArrayToBitmap(byte[] array)
    {
        if (null == array)
        {
            return null;
        }
        
        Bitmap bitmap = null;
        
        try
        {
            bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return bitmap;
    }

    /**
     * Convert a bitmap to a byte array.
     * 
     * @param bitmap   The bitmap that to be convert.
     * 
     * @return          The byte array.
     */
    public static byte[] bitampToByteArray(Bitmap bitmap)
    {
        byte[] array = null;
        ByteArrayOutputStream os = null;
        try 
        {
            if (null != bitmap)
            {
                os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, os);
                array = os.toByteArray();
            }
        } 
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != os )
            {
                try 
                {
                    os.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return array;
    }

    /**
     * Save file to Android file system.
     * 
     * @param context The Android context.
     * @param bmp     The Bitmap to be saved.
     * @param name    The name of file.
     */
    public static void saveBitmapToFile(Context context, Bitmap bmp, String name)
    {
        if (null != context && null != bmp && null != name && name.length() > 0)
        {
            FileOutputStream fos = null;
            try
            {
                fos = context.openFileOutput(name, Context.MODE_WORLD_WRITEABLE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch (OutOfMemoryError e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if( null != fos )
                {
                    try 
                    {
                        fos.close();
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Save bitmap to the a specified file path.
     * 
     * @param bmp        The Bitmap to be saved.
     * @param destFile    The file object which bitmap will be save to.
     */
    public static void saveBitmapToFile(Bitmap bmp, File destFile)
    {
        if (null != bmp && null != destFile && !destFile.isDirectory())
        {
            FileOutputStream fos = null;
            
            try
            {
                fos = new FileOutputStream(destFile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            catch (OutOfMemoryError e)
            {
                e.printStackTrace();
            }
            finally
            {
                if( null != fos )
                {
                    try 
                    {
                        fos.close();
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength,
            int maxNumOfPixels)
    {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8)
        {
            roundedSize = 1;
            //while ((roundedSize << 1 ) < initialSize)
            while ((roundedSize) < initialSize)
            {
                roundedSize <<= 1;
            }
        }
        else
        {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    /**
     * computeInitialSampleSize
     * 
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels)
    {
        double w = options.outWidth;
        double h = options.outHeight;

        final int UNCONSTRAINED = -1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound)
        {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED))
        {
            return 1;
        }
        else if (minSideLength == UNCONSTRAINED)
        {
            return lowerBound;
        }
        else
        {
            return upperBound;
        }
    }
    
    /**
     * Get the appropriate sample size from the input stream.
     * 
     * @param is The input stream.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * 
     * @return the sample size.
     */
    public static int getAppropriateSampleSize(InputStream is, int minSideLength, int maxNumOfPixels)
    {
        int sampleSize = 1;
        
        try
        {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);
            
            if (opts.outHeight > 0 && opts.outWidth > 0)
            {
                sampleSize = computeSampleSize(opts, minSideLength, maxNumOfPixels);
            }
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return sampleSize;
    }
    
    /**
     * Load bitmap from Android file system.
     * 
     * @param context The Android context.
     * @param name    The file name.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * 
     * @return The bitmap to be loaded.
     */
    public static Bitmap loadBitmapFromFile(Context context, String name, int minSideLength, int maxNumOfPixels)
    {
        Bitmap bmp = null;
        FileInputStream fis = null;
        try
        {
            if (null != context && null != name && name.length() > 0)
            {
                fis = context.openFileInput(name);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
                fis.close();
                fis = null;
                
                fis = context.openFileInput(name);
                bmp = BitmapFactory.decodeStream(fis, null, opts);
                fis.close();
                fis = null;
            }
        }
        catch(OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != fis )
            {
                try 
                {
                    fis.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }

        return bmp;
    }

    /**
     * Load bitmap from SD Card.
     * 
     * @param strPath   The path of the bitmap.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * 
     * @return The object of bitmap.
     */
    public static Bitmap loadBitmapFromSDCard(String strPath, int minSideLength, int maxNumOfPixels)
    {
        File file = new File(strPath);
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
            fis.close();
            fis = null;
            
            fis = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;
            
            return bmp;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != fis )
            {
                try 
                {
                    fis.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }

    /**
     * Save the bitmap to sd card.
     * 
     * @param bmp     The bitmap object to be saved.
     * @param strPath The bitmap file path, contains file extension.
     */
    public static Drawable bitmapToDrawable(Bitmap bmp)
    {
        if (null == bmp)
        {
            return null;
        }

        return new BitmapDrawable(bmp);
    }
    
    /**
     * Load bitmap file from sd card.
     * 
     * @param strPath The bitmap file path.
     * 
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable)
    {
        if (null == drawable)
        {
            return null;
        }
        
        int width  = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        
        return drawableToBitmap(drawable, width, height);
    }
    
    /**
     * Load bitmap file from sd card.
     * 
     * @param strPath The bitmap file path.
     * 
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height)
    {
        if (null == drawable || width <= 0 || height <= 0)
        {
            return null;
        }
        
        Config config = (drawable.getOpacity() != PixelFormat.OPAQUE) ? Config.ARGB_8888 : Config.RGB_565;
        
        Bitmap bitmap = null;
        
        try
        {
            bitmap = Bitmap.createBitmap(width, height, config);
            if (null != bitmap)
            {
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
            }
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return bitmap;
    }
    
    /**
     * Save bitmap to SD Card.
     * 
     * @param bmp       The bitmap object.
     * @param strPath   The path of the bitmap relative to SD Card.
     */
    public static void saveBitmapToSDCard(Bitmap bmp, String strPath)
    {
        if (null != bmp && null != strPath && !strPath.equalsIgnoreCase(""))
        {
            File file = new File(strPath);
            saveBitmapToFile(bmp, file);
        }
    }

    /**
     * Decode bitmap from Uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param strUir  The Uri path.
     * 
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, String strUri)
    {
        InputStream is = null;
        try
        {
            is = context.getContentResolver().openInputStream(Uri.parse(strUri));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, options);

            return bmp;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != is )
            {
                try 
                {
                    is.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Decode bitmap from Uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param uri     The Uri path.
     * 
     * @return The bitmap of the specified Uri.
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri)
    {
        InputStream is = null;
        try
        {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            
            return bmp;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != is )
            {
                try 
                {
                    is.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get bitmap thumb from uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param uri     The Uri path.
     * 
     * @return The thumbnaim of bitmap.
     */
    public static Bitmap getBitmapThumbFromUri(Context context, Uri uri)
    {
        try
        {
            long id = -1;
            // Get the image id from the uri path.
            String strPath = uri.getPath();
            int index = strPath.lastIndexOf("/");
            if (index >= 0 && index < strPath.length())
            {
                String strId = strPath.substring(index + 1);
                id = Integer.parseInt(strId);
            }
            
            if (-1 != id)
            {
                // Get the image thumb from the content thumb.
                ContentResolver cr = context.getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                
                Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
                        cr, 
                        id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        options);
                
                return thumb;
            }
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get bitmap thumb from path.
     * 
     * @param context The context object used to get content resolver.
     * @param strUri  The Uri path string.
     * 
     * @return The thumbnail of bitmap.
     */
    public static Bitmap getBitmapThumbFromString(Context context, String strUri)
    {
        if (null == strUri || 0 == strUri.length())
        {
            return null;
        }
        
        return getBitmapThumbFromUri(context, Uri.parse(strUri));
    }
    
    /**
     * Load the bitmap from the full path. In this method, we will calculate the bitmap
     * factory options sample size.
     * 
     * @param fullPath The full path of an image file.
     * 
     * @return the bitmap of the image file.
     */
    public static Bitmap loadBitmapFromFullPath(String fullPath)
    {
        return loadBitmapFromFullPath(fullPath, -1, 800 * 480 *2);
    }
    
    /**
     * Load the bitmap from the full path. In this method, we will calculate the bitmap
     * factory options sample size.
     * 
     * @param fullPath The full path of an image file.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * 
     * @return the bitmap of the image file.
     */
    public static Bitmap loadBitmapFromFullPath(String fullPath, int minSideLength, int maxNumOfPixels)
    {
        if (null == fullPath || fullPath.isEmpty())
        {
            return null;
        }
     
        Bitmap bmp = null;
        
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(fullPath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            
            opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
            fis.close();
            fis = null;
            
            fis = new FileInputStream(fullPath);
            bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != fis )
            {
                try 
                {
                    fis.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return bmp;
    }
    
    public static Bitmap loadBitmapFromFullPath(String fullPath, int inSampleSize)
    {
        if (null == fullPath || fullPath.isEmpty())
        {
            return null;
        }
     
        Bitmap bmp = null;
        
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(fullPath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            
            opts.inSampleSize = inSampleSize;
            fis.close();
            fis = null;
            
            fis = new FileInputStream(fullPath);
            bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
        }
        finally
        {
            if( null != fis )
            {
                try 
                {
                    fis.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return bmp;
    }
    
    /**
     * Load the bitmap from the full path. If error occured, throw exception.  
     * 
     * @param fullPath The full path of an image file.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * 
     * @return the bitmap of the image file.
     * @throws FileNotFoundException  Thrown when a file specified by a program cannot be found.
     * @throws IOException            Signals a general, I/O-related error.
     * @throws OutOfMemoryError       Thrown when a request for memory is made that can not be 
     *                                satisfied using the available platform resources.
     */
    public static Bitmap loadBitmapFromFullPathWithException(String fullPath, int minSideLength, int maxNumOfPixels)
            throws FileNotFoundException, IOException, OutOfMemoryError
    {
        if (null == fullPath || fullPath.isEmpty())
        {
            return null;
        }

        FileInputStream fis = null;
        Bitmap bmp = null;
        try
        {
            fis = new FileInputStream(fullPath);
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
            fis.close();
            fis = null;

            fis = new FileInputStream(fullPath);
            bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;
        }
        catch(FileNotFoundException ex)
        {
            throw ex;
        }
        catch(IOException ex)
        {
            throw ex;
        }
        catch(OutOfMemoryError ex)
        {
            throw ex;
        }
        finally
        {
            if( null != fis )
            {
                try 
                {
                    fis.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        
        return bmp;
    }
    
    /**
     * Get bitmap from resource.
     * 
     * @param context The application environment.
     * @param id      The resource id.
     * 
     * @return If success, return the bitmap; otherwise, return null.
     */
    public static Bitmap loadBitmapFromResource(Context context, int id)
    {
        if (null != context)
        {
            Drawable drawable = context.getResources().getDrawable(id);
            return BitmapUtil.drawableToBitmap(drawable);
        }
        
        return null;
    }
    
    /**
     * Set the BitmapFactory.Options.inImageImprovement options for BitmapFactory.Options.
     * It's only used in BYD's BSP version.
     * 
     * @param opts                  The options instance.
     * @param bInImageImprovement   True or false we need to set.
     *  
     * @return If set succeed, return true; otherwise, return false.
     */
    public static boolean setInImageImprovement(BitmapFactory.Options opts, boolean bInImageImprovement)
    {
        if (null == opts)
        {
            return false;
        }
        
        boolean bSuccess = false;
        
        try
        {
            Field field = opts.getClass().getDeclaredField("inImageImprovement");
            field.setAccessible(true);
            field.setBoolean(opts, bInImageImprovement);
            
            return true;
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return bSuccess;
    }
}
