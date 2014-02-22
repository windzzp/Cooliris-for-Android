package com.gtx.cooliris.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.interfaces.IAsyncImageView;

public class ImageGroupEx extends ImageView implements IAsyncImageView {
	private static int s_textPx = 
		(int)(CoolirisApplication.getAppContext().getResources().getDisplayMetrics().scaledDensity * 14);
	private static int LR_PADDING = 2;
	private static int TB_PADDING = 2;

	private ColorDrawable 	m_colorDrawable = null;
	private Paint 			m_textPaint 	= null;
	private int 			m_textHeight 	= 20;
	private int 			m_coverHeight 	= 20;
	private Rect 			m_coverBounds 	= null;
	private boolean 		m_isDisplayThumb= false;

	public ImageGroupEx(Context context) {
		this(context, null);
	}

	public ImageGroupEx(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageGroupEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		s_textPx = (int) spTopixels(context, 14);

		m_colorDrawable = new ColorDrawable(0x30000000);
		m_textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
		m_textPaint.setColor(0xFFFFFFFF);
		m_textPaint.setTextSize(s_textPx);
		m_textPaint.setTextAlign(Align.LEFT);
		m_textPaint.setAntiAlias(true);

		// Rect bounds = new Rect();
		// m_textPaint.getTextBounds("a", 0, 1, bounds);
		// m_textHeight = bounds.height();
		m_textHeight = s_textPx;
		m_coverHeight = m_textHeight + TB_PADDING * 2;

		m_coverBounds = new Rect(0, 0, 0, 0);
	}

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

	@Override
	public void setImageBitmap(Bitmap bmp) {
		/*
		 * if (null != bmp) { m_aspectRatio = (float)bmp.getWidth() /
		 * (float)bmp.getHeight(); }
		 */

		super.setImageBitmap(bmp);
	}

	@Override
	public void setBackgroundDrawable(Drawable drawable) {
		super.setBackgroundDrawable(drawable);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
	}

	@Override
	public Drawable getDrawable() {
		return super.getDrawable();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int w = getWidth();
		final int h = getHeight();

		m_coverBounds.set(0, h - m_coverHeight, w, h);
		m_colorDrawable.setBounds(m_coverBounds);
		m_colorDrawable.draw(canvas);

		canvas.drawText(
				((ImageGroup) m_data).getTitle(), 
				LR_PADDING, 
				(h - m_coverHeight + TB_PADDING + s_textPx - TB_PADDING),
				m_textPaint);
		// drawText(canvas, m_textPaint, m_coverBounds,
		// ((Girl)m_data).getTitleAbstract(), TB_PADDING, TB_PADDING, false,
		// true);

		// canvas.drawText(((Girl)m_data).getTitleAbstract(), 5, 5,
		// m_textPaint);
	}

	public static float pixelsToSp(Context context, Float px) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return px / scaledDensity;
	}

	public static float spTopixels(Context context, int sp) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return sp * scaledDensity;
	}

	/**
	 * Draw the text in specified rectangle.
	 * 
	 * @param canvas
	 *            The canvas on which text is drawn.
	 * @param bounds
	 *            The bounds of text, the text which is out of the bound will be
	 *            clipped.
	 * @param text
	 *            The text to be drawn.
	 */
	protected void drawText(Canvas canvas, Paint textPaint, Rect bounds,
			String text, int marginLR, int marginTop, boolean center,
			boolean isSingleLine) {
		if (null == text || 0 == text.length() || null == bounds
				|| bounds.isEmpty()) {
			return;
		}

		float[] measuredWidth = new float[1];
		int start = 0;
		int end = text.length();
		int index = 0;
		int width = bounds.width() - marginLR * 2;
		int height = bounds.height();
		int lines = isSingleLine ? 1 : getTextLines(text, width);

		// Calculate text height of one line
		Rect txtRc = new Rect();
		textPaint.getTextBounds(text, start, end, txtRc);
		int lineH = /* TEXT_LINESPACE + */txtRc.height();

		int startX = bounds.left;
		int startY = (height - lineH * lines) / 2;
		if (startY < 0) {
			startY = bounds.bottom - height;
		} else {
			startY += bounds.bottom - height;
		}

		if (Math.abs(bounds.top - startY) < marginTop) {
			startY = bounds.top + marginTop;
		}

		canvas.save();
		canvas.clipRect(bounds);

		while (true) {
			// The line height is bigger than the bounds height.
			if (lineH > height) {
				break;
			}

			startY += lineH;
			// Break text to measure.
			index = textPaint.breakText(text, start, end, true, width,
					measuredWidth);
			// The start X for one line text.
			startX = bounds.left + marginLR;
			// Make the text be center in the bounds.
			if (center) {
				startX += (width - (int) measuredWidth[0]) / 2;
			}

			// The next line can not be display fully.
			if (startY + lineH >= bounds.bottom) {
				// The last line.
				if (start + index == end) {
					canvas.drawText(text, start, start + index, startX, startY, textPaint);
				} else {
					// Draw text with the ellipsis.
					String str = text.substring(start, start + index - 2) + "...";
					canvas.drawText(str, 0, str.length(), startX, startY, textPaint);
				}

				break;
			} else {
				canvas.drawText(text, start, start + index, startX, startY, textPaint);
			}

			start += index;

			// The end of text.
			if (start == end) {
				break;
			}
		}

		canvas.restore();
	}

	/**
	 * Get the text lines in specified maximum width.
	 * 
	 * @param width
	 *            The width of text bounds.
	 * 
	 * @return the lines of text which breaks by the maximum bounds.
	 */
	protected int getTextLines(String text, int width) {
		if (null == text || 0 == text.length()) {
			return 0;
		}

		float[] measuredWidth = new float[1];
		int start = 0;
		int end = text.length();
		int index = 0;
		int lines = 0;

		while (true) {
			index = m_textPaint.breakText(text, start, end, true, width,
					measuredWidth);
			lines++;
			start += index;

			// The end of text.
			if (start == end) {
				break;
			}
		}

		return lines;
	}
}
