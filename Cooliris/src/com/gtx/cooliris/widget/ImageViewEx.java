package com.gtx.cooliris.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gtx.cooliris.interfaces.IAsyncImageView;

public class ImageViewEx extends RecyclingImageView implements IAsyncImageView {
	private boolean m_isDisplayThumb = false;

	public ImageViewEx(Context context) {
		super(context);
	}

	public ImageViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

/*	public ImageViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}*/

	Object m_data = null;

	@Override
	public void setDataSource(Object dataSource) {
		m_data = dataSource;
	}

	@Override
	public Object getDataSource() {
		return m_data;
	}

	@Override
	public void setIsDisplayThumb(boolean isDisplayThumb) {
		m_isDisplayThumb = isDisplayThumb;
	}

	@Override
	public boolean isDisplayThumb() {
		return m_isDisplayThumb;
	}
}
