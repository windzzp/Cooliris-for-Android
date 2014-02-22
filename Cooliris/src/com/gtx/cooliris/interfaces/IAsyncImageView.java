package com.gtx.cooliris.interfaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface IAsyncImageView
{
	public void setDataSource(Object dataSource);
	public Object getDataSource();
	
	public void setIsDisplayThumb(boolean isDisplayThumb);
	public boolean isDisplayThumb();
	
	public void setImageBitmap(Bitmap bmp);
	public void setBackgroundDrawable(Drawable drawable);
	//public void setBackgroundColor(int color);
	
	public void setImageDrawable(Drawable drawable);
	public Drawable getDrawable();
}
