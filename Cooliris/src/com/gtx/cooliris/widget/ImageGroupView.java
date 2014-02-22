package com.gtx.cooliris.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.interfaces.IAsyncImageView;
import com.gtx.cooliris.R;

public class ImageGroupView extends FrameLayout implements IAsyncImageView {
	private ImageGroup 	m_imageGroup 	= null;
	private RecyclingImageView 	m_imageView 	= null;
	private TextView 	m_title 		= null;
	private boolean 	m_isDisplayThumb= false;

	public ImageGroupView(Context context) {
		this(context, null);
	}

	public ImageGroupView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageGroupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initialize(context);
	}

	@Override
	public void setDataSource(Object dataSource) {
		if (dataSource instanceof ImageGroup) {
			m_imageGroup = (ImageGroup) dataSource;

			if (CoolirisApplication.needShowDescription()) {
				setTextView();
			} else {
				if (null != m_title) {
					m_title.setVisibility(View.GONE);
				}
			}
		} else {
			m_title.setVisibility(View.GONE);
		}
	}

	@Override
	public Object getDataSource() {
		return m_imageGroup;
	}

	@Override
	public void setIsDisplayThumb(boolean isDisplayThumb) {
		m_isDisplayThumb = isDisplayThumb;
	}

	@Override
	public boolean isDisplayThumb() {
		return m_isDisplayThumb;
	}

	@Override
	public void setImageBitmap(Bitmap bmp) {
		m_imageView.setImageBitmap(bmp);
	}

	@Override
	public void setBackgroundDrawable(Drawable drawable) {
		m_imageView.setBackgroundDrawable(drawable);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		m_imageView.setImageDrawable(drawable);
		// setTextView();
	}

	@Override
	public Drawable getDrawable() {
		return m_imageView.getDrawable();
	}

	public void setScaleType(ScaleType scaleType) {
		m_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.image_grid_item, this);

		m_imageView = (RecyclingImageView) findViewById(R.id.girl_imageview);
		m_title = (TextView) findViewById(R.id.girl_abstract_title);

		if (!CoolirisApplication.needShowDescription()) {
			m_title.setVisibility(View.GONE);
		}
	}

	private void setTextView() {
		if (null != m_imageGroup && null != m_title) {
			m_title.setVisibility(View.VISIBLE);
			m_title.setText(m_imageGroup.getTitle());
		}
	}
}
